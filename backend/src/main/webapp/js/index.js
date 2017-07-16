
// now we need ability to go back.
// either keep tree in javascript of where we going each time. or do requests.
// lets do online.
// so we have our parent guy. we need to get his parent and it's children


//server:client_id:
var CLIENT_ID = "582892993246-g35aia2vqj3dl9umucp57utfvmvt57u3.apps.googleusercontent.com"
var SCOPES = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/tasks"
var API_KEY = "AIzaSyD81UMLvDPOz_gOov_9fuaWZopCNwWrS-4"

function Parent(uuid, kind) {
    this.uuid = uuid
    this.kind = kind
}
Parent.prototype.isTask = function() {
    return this.kind == "Task"
}



$(function() {

    viewModel = {
        itemList: ko.observableArray(),
        parent: ko.observable(new Parent(null, null)),
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
        showRemoved: ko.observable(false)
    };

    viewModel.apisLoaded.subscribe(function(val) {
        if (!val) return;
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

function isTask(kind) {
    return kind == "Task"
}

function completeTask(task) {
    task.completeDate = new Date()
    gapi.client.itemsApi.completeTask({"uuid": task.uuid, "completeDate": task.completeDate.toISOString()}).execute()
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
        if (isTask(t.kind)) {
            gapi.client.itemsApi.moveTask({"uuid": t.uuid, "parentUuid": t.parentUuid}).execute(callback)
        } else {
            gapi.client.itemsApi.moveItem({"uuid": t.uuid, "parentUuid": t.parentUuid}).execute(callback)
        }
    }
    cancelMove()
}

function onItemClick(item) {
    viewModel.parentPath.push(viewModel.parent())
    viewModel.parent(new Parent(item.uuid, item.kind))
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

// Get authorization from the user to access profile info
function initAuth() {
    gapi.client.setApiKey(API_KEY);
    gapi.auth2.init({
        client_id: CLIENT_ID,
        scope: SCOPES,
    }).then(function () {
        console.log("everything is good")
        auth2 = gapi.auth2.getAuthInstance();
        auth2.isSignedIn.listen(updateSigninStatus);
        updateSigninStatus(auth2.isSignedIn.get());
        $('#login').click(auth)
    });
}

function updateSigninStatus(isSignedIn) {
    if (isSignedIn) {
        console.log(auth2.currentUser.get().getBasicProfile().getGivenName());
        loadApi();
    } else {
        console.log("terrible")
    }

}

function auth() {
    auth2.signIn();
}

// this is where we starting out
function init() {
    gapi.load('client:auth2', initAuth);
}

function loadApi() {
    var apiName = 'itemsApi';
    var apiVersion = 'v3';
    var apiRoot = 'https://' + window.location.host + '/_ah/api';
    if (window.location.hostname == 'localhost'
      || window.location.hostname == '127.0.0.1'
      || ((window.location.port != "") && (window.location.port > 1023))) {
        // We're probably running against the DevAppServer
      apiRoot = 'http://' + window.location.host + '/_ah/api';
    }
    //apiRoot = "https://antoshkaplus-words.appspot.com/_ah/api"


    gapi.client.load(apiName, apiVersion, undefined, apiRoot).then(
        function(response) {
            viewModel.itemsApiLoaded(true)
            gapi.client.itemsApi.getRootUuid().execute(function(resp) {
                if (resp.error != null) {
                    console.log("error getRootUuid", resp)
                    return
                }
                console.log(resp.uuid)
                // root is always item
                viewModel.parent(new Parent(resp.uuid, "Item"))
                console.log("root uuid set")
            })
            console.log("api loaded")
        },
        function(reason) {
            console.log("api load failure", reason)
        })
    gapi.client.load('tasks', 'v1', function(resp) { viewModel.gtaskApiLoaded(true); });

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
    gapi.client.itemsApi.addItem({task: item}).execute()

}

function addItem() {
    if (viewModel.parent().isTask()) throw "Can't insert Item into Task."
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

function convertVariantItems(variantItems) {
    return variantItems.map(function(x) { return x.item ? x.item : x.task; });
}

function toVariantItems(items) {
    return items.map(function(x) { return isTask(x) ? {task: x} : {item: x} })
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

        viewModel.itemList(convertVariantItems(resp.variantItems))
        console.log(resp)
    })
}
