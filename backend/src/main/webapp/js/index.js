
function itemCssClass(item) {
    cssClass = ""
    if (item.disabled) cssClass += 'g-deleted '
    cssClass += (item.kind == "Task" ? "task" : "item");
    return cssClass
}

function showCurrent(item) {
    $("components-navigation")[0].show(item)
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

