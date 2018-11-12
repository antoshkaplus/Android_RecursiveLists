
var VM = null;

(function () {

    // has to go inside constructor

    var vm = {
        addItemValue: ko.observable(),
        selectedItems: ko.observableArray(),
        itemList: ko.observableArray(),
        parent: ko.observable({title:null, kind:null, uuid:null}),
        parentPath: [],
        gTasks: ko.observableArray(),
        showDeleted: ko.observable(false),
        showCompleted: ko.observable(false),
        showMoved: ko.observable(false),
        // TODO setting to pick default distribution
        showUnmarked: ko.observable(true),
        selectedItems: ko.observableArray(),
        waitRelease: ko.observable(false),
        moveItems: [],
        moveParent: "",
        prepareMove: ko.observable(false),
        preparedItems: ko.observableArray(),
        showRemoved: ko.observable(false),
        editingTitle: ko.observable(false),
        editedTitle: ko.observable(),

        addItem: function(data, event) {
            if (isTask(vm.parent())) throw "Can't insert Item into Task."
            var item = new Item(vm.addItemValue(), vm.parent().uuid)
            item.kind = "Item"
            itemsRepo.item.add(item);
        },
        addTask: function() {
            var item = new Item(vm.addItemValue(), vm.parent().uuid)
            item.kind = "Task"
            itemsRepo.task.add(item);
        },
        showGtask: function(gtask) {
            g = gtask;
            return (vm.showUnmarked() && !g.deleted && !g.moved && !g.completed) ||
                    (vm.showDeleted() && g.deleted) || (vm.showMoved() && g.moved) ||
                    (vm.showCompleted() && g.completed);
        },
        setPrepareMove: function() {
            vm.prepareMove(true)
        },
        back: function() {
            if (vm.parentPath.length == 0) return;
            p = vm.parentPath.pop()
            vm.parent(p)
        },
        // we may want to disable other operations
        moveSelected: function() {
            vm.waitRelease(true)
            vm.moveItems = vm.selectedItems().slice()
            vm.itemList.removeAll(vm.moveItems)
            vm.selectedItems.removeAll()
            vm.moveParent = vm.parent()

            // no server side handling
        },
        removeSelected: function() {
            params = {variantItems: toVariantItems(vm.selectedItems())}
            vm.selectedItems.removeAll();
            gapi.client.itemsApi.removeItemList(params).then(
                function(resp) {
                    console.log("remove success")
                },
                function(reason) {
                    console.log("remove failure")
                });
        },
        cancelMove: function() {
            vm.waitRelease(false)
            vm.moveItems = null
            vm.moveParent = ""
        },
        releaseMove: function() {
            // so here we actually move selectedItems to new parent
            var parentUuid = vm.parent().uuid
            var items = vm.moveItems
            for (var i = 0; i < items.length; ++i) {
                var t = items[i]
                function callback(resp) {
                    if (resp.error != null) {
                        console.log("move error", resp)
                    } else {
                        console.log("sucksessful move")
                    }
                }

                t.parentUuid = parentUuid
                if (isTask(t)) {
                    gapi.client.itemsApi.moveTask({"uuid": t.uuid, "parentUuid": t.parentUuid}).execute(callback)
                } else {
                    gapi.client.itemsApi.moveItem({"uuid": t.uuid, "parentUuid": t.parentUuid}).execute(callback)
                }
            }
            vm.cancelMove()
        },
        pushParent: function(item) {
            vm.parentPath.push(vm.parent())
            vm.parent(item)
        },
        getRootItem: function() {
            if (!vm.parentPath.length) return vm.parent()
            return vm.parentPath[0]
        },
        showCurrent: function(task) {
            root = vm.getRootItem()
            if (root.uuid == task.parentUuid) {
                // it's root
                vm.pushParent(root)
                $('.nav-tabs a[href="#navigation"]').tab('show')
                return
            }
            gapi.client.itemsApi.getItem({uuid: task.parentUuid}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result);
                    vm.pushParent(convertVarItem(resp.result))
                    $('.nav-tabs a[href="#navigation"]').tab('show')
                },
                function(reason) {
                    console.log("error", reason)
                })
        },
        listTaskLists: function() {
            gTasks = vm.gTasks

            Gtask.forEachList({fields: "items(id,title)"}, function(taskList) {

                var obj = new GoogleTaskList(taskList)
                gTasks.push(ko.observable(obj));

                var options = {
                    tasklist: taskList.id,
                    fields: "nextPageToken,items(id,deleted,completed,updated,status,title)",
                    showDeleted: true,
                    showHidden: true
                };
                Gtask.forTasks(options, function(tasks) {
                    if (!tasks) return;

                    tasks.forEach(function(t) { t.listId = taskList.id; })
                    var ids = tasks.map(function(t) { return t.id });
                    gapi.client.itemsApi.checkGtaskIdPresent({ids : ids}).then(function(resp) {
                        if (!Array.isArray(resp.result.ids)) return;

                        var ids = resp.result.ids
                        for (var j = 0; j < tasks.length; ++j) {
                            tasks[j].moved = (ids[j] != null);
                            obj.tasks.push(tasks[j])
                        }
                        obj.tasks.sort(function (left, right) { return new Date(right.updated) - new Date(left.updated); });
                    });
                })
            });
        },
        appendPre: function(message) {
            var pre = document.getElementById('output');
            var textContent = document.createTextNode(message + '\n');
            pre.appendChild(textContent);
        },
        initItemListRoot: function() {
            gapi.client.itemsApi.getRootUuid().execute(function(resp) {
                if (resp.error != null) {
                    console.log("error getRootUuid", resp)
                    return
                }
                console.log(resp.uuid)
                // root is always item
                vm.parent({kind:item, uuid:resp.uuid, title:"Root"})
                console.log("root uuid set")
            })
        },
        fillItemList: function() {
            gapi.client.itemsApi.getChildrenItems({parentUuid: vm.parent().uuid}).execute(function(resp) {
                if (resp.error != null) {
                    // need to show some kind of sign to reload browser window
                    // later on may try to reload by myself
                    console.log("error happened", resp)
                    return
                }
                // empty items with such parent
                if (!resp.variantItems) resp.variantItems = []

                items = convertVariantItems(resp.variantItems);
                items.forEach(convertItemRecordInplace)
                vm.itemList(filterOldCompletedTasks(items))
            })
        },
        refreshItemList: function() {
            list = vm.itemList().slice()
            vm.itemList.removeAll()
            vm.itemList(list)
        },

        movePrepared: function() {
            gapi.client.itemsApi.addGtaskList({gtasks: vm.preparedItems(), parentUuid: vm.parent().uuid}).then(
                function(resp) {
                    vm.preparedItems().forEach(function(entry) {
                        entry.moved = true
                    });
                    vm.preparedItems([])
                    vm.prepareMove(false)

                    vm.updateGtasksVisualization()
                },
                function(reason) {
                    console.log("unable to move gtask items")
                });
        },
        deletePrepared: function() {
            s = new Map()
            vm.preparedItems().forEach(function(item) {
                if (!s.has(item.listId)) s.set(item.listId, [item.id])
                else s.get(item.listId).push(item.id)
            })
            s.forEach(function(itemIds, listId) {
                callback = function(p) {
                    vm.preparedItems().forEach(function(entry) {
                        entry.deleted = true
                    });
                    vm.preparedItems([])
                    vm.prepareMove(false)
                    console.log(p, 'success delete')

                    vm.updateGtasksVisualization()
                }
                errorHandler = function(p) { console.log(p, 'failure delete') }

                Gtask.deleteTasks(listId, itemIds, callback, errorHandler)
            })
        },
        updateGtasksVisualization: function() {
            var data = vm.gTasks().slice(0);
            vm.gTasks([]);
            vm.gTasks(data);
        },

        setEditingTitle: function() {
            vm.editingTitle(true);
            $('#editTitleInput').focus().select()
        },
        resetEditingTitle: function() {
            vm.editingTitle(false);
            // send whatever value is there to the server
            // maybe keep the previous value for sometime
        }
    }

    vm.parent.subscribe(vm.fillItemList)
    vm.parent.subscribe(() => vm.selectedItems.removeAll(), null, "beforeChange")

    externalApis.allLoaded.subscribe(function(val) {
        if (!val) return;
        vm.initItemListRoot()
        vm.listTaskLists()
    })

    itemsRepo.pubs.item.add.subscribe(function(newItem) {
        if (vm.parent().uuid == newItem.parentUuid) {
            vm.itemList.unshift(newItem);
        }
    });
    itemsRepo.pubs.task.add.subscribe(function(newTask) {
        if (vm.parent().uuid == newTask.parentUuid) {
            vm.itemList.unshift(newTask);
        }
    });
    itemsRepo.pubs.task.update.subscribe(function(task) {
        listTask = vm.itemList().find(item => item.uuid == task.uuid);
        if (listTask) {
            vm.itemList.replace(listTask, task)
        }
    });

    var thatDoc = document;
    var thisDoc = document.currentScript.ownerDocument

    var tmpl = thisDoc.querySelector('template')
    var Element = Object.create(HTMLElement.prototype)

    Element.createdCallback = function () {
        var clone = thatDoc.importNode(tmpl.content, true);
        this.appendChild(clone);

        this.show = function(item) {
            vm.showCurrent(item);
        }

        ko.applyBindings(vm, this)
    }

    thatDoc.registerElement('components-navigation', {prototype: Element});

    VM = vm;
})()