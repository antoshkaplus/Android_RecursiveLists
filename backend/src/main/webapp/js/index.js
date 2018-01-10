

$(function() {

    viewModel = {
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
        gtaskApiLoaded: ko.observable(false),
        itemsApiLoaded: ko.observable(false),
        apisLoaded: ko.pureComputed(function() {
            return viewModel.gtaskApiLoaded() && viewModel.itemsApiLoaded();
        }),
        prepareMove: ko.observable(false),
        preparedItems: ko.observableArray(),
        showRemoved: ko.observable(false),
        currentTasks: ko.observable([]),
        allTasks: ko.observable([])
    };

    viewModel.apisLoaded.subscribe(function(val) {
        if (!val) return;
        initItemListRoot()
        fillCurrentTasks()
        fillAllTasks()
        listTaskLists()
    });

    viewModel.parent.subscribe(fillItemList)
    viewModel.parent.subscribe(function (val) {

        viewModel.selectedItems.removeAll()

    }, null, "beforeChange")

    ko.applyBindings(viewModel)
})

function itemCssClass(item) {
    cssClass = ""
    if (item.disabled) cssClass += 'g-deleted '
    cssClass += (item.kind == "Task" ? "task" : "item");
    return cssClass
}

function showGtask(gtask) {
    vm = viewModel;
    g = gtask;
    return (vm.showUnmarked() && !g.deleted && !g.moved && !g.completed) ||
            (vm.showDeleted() && g.deleted) || (vm.showMoved() && g.moved) || (vm.showCompleted() && g.completed);
}


function setPrepareMove() {
    viewModel.prepareMove(true)
}

function itemKindClass(kind) {
    return kind == "Task" ? "task" : "item";
}

function isTask(t) {
    return t.kind == "Task"
}

function completeTask(task) {
    task.completeDate = new Date()
    gapi.client.itemsApi.completeTask({"uuid": task.uuid, "completeDate": task.completeDate.toISOString()}).then(
        function(resp) {
            console.log("completeTask complete")

        },
        function(error) {
            console.log("completeTask failure")
        }
    )
}

function makeCurrent(task) {
    task.current = true
    gapi.client.itemsApi.setCurrentTask({"uuid": task.uuid, "current": true}).then(
        function(resp) {
            refreshItemList()
            viewModel.currentTasks(viewModel.currentTasks().concat([task]))
            console.log("task made current")
        }, 
        function(error) {
            console.log("task is not made current. failure.")
        })
}

function removeCurrent(task) {
    task.current = false
    gapi.client.itemsApi.setCurrentTask({"uuid": task.uuid, "current": false}).then(
        function(resp) {
            viewModel.currentTasks(viewModel.currentTasks().filter(x => x !== task))
            console.log("task remove current. success.")
        },
        function(error) {
            console.log("task remove current. failure.")
        }
    )
}

function back() {
    if (viewModel.parentPath.length == 0) return;
    p = viewModel.parentPath.pop()
    viewModel.parent(p)
}

// we may want to disable other operations
function moveSelected() {
    viewModel.waitRelease(true)
    viewModel.moveItems = viewModel.selectedItems().slice()
    viewModel.itemList.removeAll(viewModel.moveItems)
    viewModel.selectedItems.removeAll()
    viewModel.moveParent = viewModel.parent()
}

function removeSelected() {
    params = {variantItems: toVariantItems(viewModel.selectedItems())}
    viewModel.selectedItems.removeAll();
    gapi.client.itemsApi.removeItemList(params).then(
        function(resp) {
            console.log("remove success")
        },
        function(reason) {
            console.log("remove failure")
        });
}


function cancelMove() {
    viewModel.waitRelease(false)
    viewModel.moveItems = null
    viewModel.moveParent = ""
}

function releaseMove() {
    // so here we actually move selectedItems to new parent
    var parentUuid = viewModel.parent().uuid
    var items = viewModel.moveItems
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
    cancelMove()
}

function pushParent(item) {
    viewModel.parentPath.push(viewModel.parent())
    viewModel.parent(item)
}

function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

function getRootItem() {
    if (!viewModel.parentPath.length) return viewModel.parent()
    return viewModel.parentPath[0]
}

function showCurrent(task) {
    root = getRootItem()
    if (root.uuid == task.parentUuid) {
        // it's root
        pushParent(root)
        $('.nav-tabs a[href="#navigation"]').tab('show')
        return
    }
    gapi.client.itemsApi.getItem({uuid: task.parentUuid}).then(
        function(resp) {
            convertItemRecordInplace(resp.result);
            pushParent(convertVarItem(resp.result))
            $('.nav-tabs a[href="#navigation"]').tab('show')
        },
        function(reason) {
            console.log("error", reason)
        })
}

function GoogleTaskList(taskList) {
    this.updated = taskList.updated
    this.showTasks = ko.observable(false)
    this.tasks = ko.observableArray()
    this.title = taskList.title
    this.id = taskList.id
}
GoogleTaskList.prototype.toggleShowTasks = function () {
    this.showTasks(!this.showTasks())
}

function GoogleTask(title) {
    this.updated = new Date()
    this.title = title
    this.tasklist = "@default"
    this.fields = "id,updated"
}


function restoreGtask(gtask) {
    var self = this
    gapi.client.tasks.tasks.patch({tasklist: this.id, task: gtask.id, deleted: false}).then(function(resp) {
        if (resp.code) {
            console.log("unable to update", resp)
            return
        }
        gtask.deleted = false
        var data = self.tasks().slice(0);
        self.tasks([]);
        self.tasks(data);
    })
}


function addGoogleTask() {
    title = $('#googleTask').val()

    // i'm inserting new lists here
    // not tasks
    req = gapi.client.tasks.tasks.insert(new GoogleTask(title))
    req.then(function(resp) {
        console.log(resp.result.updated)
    })
}


function movePrepared() {
//    WHY THIS IS HERE ???
//    var d = new Date().toISOString();
//    viewModel.preparedItems().forEach(function(item) {
//         item.updated = d;
//    });
    gapi.client.itemsApi.addGtaskList({gtasks: viewModel.preparedItems(), parentUuid: viewModel.parent().uuid}).then(function(resp) {
        if (resp.code) {
            console.log("was unable to move prepared items")
            return
        }
        viewModel.preparedItems().forEach(function(entry) {
            entry.moved = true
        });
        viewModel.preparedItems([])
        viewModel.prepareMove(false)

        updateGtasksVisualization()
    });
}

function updateGtasksVisualization() {
    var data = viewModel.gTasks().slice(0);
    viewModel.gTasks([]);
    viewModel.gTasks(data);
}



function deletePrepared() {
    s = new Map()
    viewModel.preparedItems().forEach(function(item) {
        if (!s.has(item.listId)) s.set(item.listId, [item.id])
        else s.get(item.listId).push(item.id)
    })
    s.forEach(function(itemIds, listId) {
        callback = function(p) {
            viewModel.preparedItems().forEach(function(entry) {
                entry.deleted = true
            });
            viewModel.preparedItems([])
            viewModel.prepareMove(false)
            console.log(p, 'success delete')

            updateGtasksVisualization()
        }
        errorHandler = function(p) { console.log(p, 'failure delete') }

        Gtask.deleteTasks(listId, itemIds, callback, errorHandler)
    })
}


function importGoogleTasks() {
    // get last update date from our server
    gapi.client.itemsApi.getGtaskLastUpdate().execute(function(resp) {
        if (resp.code) {
            console.log(resp)
            return
        }
        if (!resp.result.hasOwnProperty('value')) {
            resp.result.value = 0
        }
        var lastUpdate = new Date(resp.result.value)
        var newLastUpdate = new Date()
        // consider that 1 hour is enough for any request (update transaction) to finish
        // on gtast server
        newLastUpdate.setHours(newLastUpdate.getHours() - 1);


        var importerVM = {
            listCount: ko.observable(0),
            listTraversed: ko.observable(0),
            error: ko.observable(false),
            taskCount: ko.observable(0),
            taskTraversed: ko.observable(0),

            updateTimestamp: ko.pureComputed(function() {
                return !importerVM.error() &&
                        importerVM.listCount() > 0 &&
                        importerVM.listCount() == importerVM.listTraversed() &&
                        importerVM.taskCount() == importerVM.taskTraversed();
            }),
        }
        importerVM.updateTimestamp.subscribe(function(val) {
            if (!val) return;

            gapi.client.itemsApi.updateGtaskLastUpdate({value: newLastUpdate}).then(function(resp) {
                if (resp.code) {
                    console.log("import gtask error")
                    return
                }
                console.log("import gtask success")
            })
        })

        function errorHandler() {
            importerVM.error(true);
        }

        Gtask.forEachList({}, function(taskList) {
            importerVM.listCount(importerVM.listCount() +1)

            if (taskList.updated > newLastUpdate) {
                newLastUpdate = taskList.updated
            }

            var options = {
                tasklist: taskList.id,
                showDeleted: true,
                showHidden: true,
                updatedMin: lastUpdate.toISOString()
            }

            Gtask.forTasks(options, function(tasks) {
                if (!tasks) {
                    importerVM.listTraversed(importerVM.listTraversed() +1)
                    return
                }

                importerVM.taskCount(importerVM.taskCount() +1)
                gapi.client.itemsApi.addGtaskList({gtasks: tasks}).then(function(resp) {
                    importerVM.taskTraversed(importerVM.taskTraversed() +1)
                    if (resp.code) {
                        console.log("was unable to import gtask list")
                        errorHandler()
                    }
                });
            }, errorHandler)

        }, errorHandler)
    })
    // get all updates with min date from g server... iterate over lists

    // gather everything and send to our server

}


function listTaskLists() {
    gTasks = viewModel.gTasks

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

                ids = resp.result.ids
                for (var j = 0; j < tasks.length; ++j) {
                    tasks[j].moved = (ids[j] != null);
                    obj.tasks.push(tasks[j])
                }
                obj.tasks.sort(function (left, right) { return new Date(right.updated) - new Date(left.updated); });
            });
        })
    });
}

function appendPre(message) {
    var pre = document.getElementById('output');
    var textContent = document.createTextNode(message + '\n');
    pre.appendChild(textContent);
}


function Item(title) {
    this.title = title
    this.createDate = timestampToSend()
    this.kind = "Item"
    this.uuid = guid()
    this.parentUuid = viewModel.parent().uuid
}

function addTask() {
    title = $('#item').val()
    item = new Item(title)
    item.kind = "Task"
    gapi.client.itemsApi.addItem({task: item}).then(
       function(resp) {
           console.log(item, "add item success")
       },
       function(reason) {
           console.log(reason, "add item failure")
       })
}

function addItem() {
    if (isTask(viewModel.parent())) throw "Can't insert Item into Task."
    title = $('#item').val()
    item = new Item(title)
    item.kind = "Item"
    gapi.client.itemsApi.addItem({item: item}).then(
        function(resp) {
            console.log(item, "add item success")
        },
        function(reason) {
            console.log(reason, "add item failure")
        })
}

function convertVarItem(x) {
    return x.item ? x.item : x.task;
}

function convertVariantItems(variantItems) {
    return variantItems.map(convertVarItem);
}

function convertItemRecordInplace(x) {
    ['createDate', 'updateDate', 'completeDate'].forEach(function(p) {
        if (x[p] && typeof x[p] === 'string') {
            x[p] = new Date(x[p]);
        }
    })
}

function toVariantItems(items) {
    return items.map(function(x) { return isTask(x) ? {task: x} : {item: x} })
}

function initItemListRoot() {
    gapi.client.itemsApi.getRootUuid().execute(function(resp) {
        if (resp.error != null) {
            console.log("error getRootUuid", resp)
            return
        }
        console.log(resp.uuid)
        // root is always item
        viewModel.parent({kind:item, uuid:resp.uuid, title:"Root"})
        console.log("root uuid set")
    })
}

function filterOldCompletedTasks(items) {
    var bound = new Date();
    bound.setMonth(bound.getMonth() - 1);
    return items.filter(function(x) { return !isTask(x) || !x.completeDate || x.completeDate > bound})
}


function fillCurrentTasks() {
    gapi.client.itemsApi.getCurrentTaskList().execute(function(resp) {
        if (resp.error != null) {
            console.log("error getCurrentTaskList", resp)
            return
        }
        if (!Array.isArray(resp.items)) {
            resp.items = []
        }
        resp.items.forEach(convertItemRecordInplace);
        viewModel.currentTasks(filterOldCompletedTasks(resp.items));
    })
}

function fillAllTasks() {
    gapi.client.itemsApi.getAllTaskList().then(
        function(resp) {
            if (!Array.isArray(resp.result.items)) {
                resp.result.items = []
            }
            resp.result.items.forEach(convertItemRecordInplace);
            viewModel.allTasks(filterOldCompletedTasks(resp.result.items));
        },
        function(reason) {
            console.log(reason, "error fillAllTasks")
        })
}

function fillItemList() {
    gapi.client.itemsApi.getChildrenItems({parentUuid: viewModel.parent().uuid}).execute(function(resp) {
        if (resp.error != null) {
            // need to show some kind of sign to reload browser window
            // later on may try to reload by myself
            console.log("error happened", resp)
            return
        }
        // empty items with such parent
        if (!resp.variantItems) resp.variantItems = []
        resp.variantItems.forEach(convertItemRecordInplace);

        viewModel.itemList(filterOldCompletedTasks(convertVariantItems(resp.variantItems)))
        console.log(resp)
    })
}

function refreshItemList() {
    list = viewModel.itemList().slice()
    viewModel.itemList.removeAll()
    viewModel.itemList(list)
}

