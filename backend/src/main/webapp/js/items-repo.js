
itemsRepo = {

    _allItems: null,

    pubs: {
        task: {
            add: new ko.subscribable(),
            update: new ko.subscribable(),
        },

        item: {
            add: new ko.subscribable()
        },

        any: {
            move: new ko.subscribable(),
            remove: new ko.subscribable()
        }
    },

    task: {
        // TODO calculate path and parent for the new guy
        add: function(task) {
            gapi.client.itemsApi.addItem({task: task}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result.task)
                    itemsRepo.pubs.task.add.notifySubscribers(resp.result.task)
                },
                function(reason) {
                    console.log("task.add.failure", reason)
                })
        },
        makeCurrent: function(task) {
            gapi.client.itemsApi.setCurrentTask({"uuid": task.uuid, "current": true}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result)
                    itemsRepo.pubs.task.update.notifySubscribers(resp.result)
                },
                function(error) {
                    console.log("task.makeCurrent.failure", error)
                })
        },
        removeCurrent: function(task) {
            gapi.client.itemsApi.setCurrentTask({"uuid": task.uuid, "current": false}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result)
                    itemsRepo.pubs.task.update.notifySubscribers(resp.result)
                },
                function(error) {
                    console.log("task remove current. failure.", error)
                }
            )
        },
        complete: function(task) {
            task.completeDate = new Date()
            gapi.client.itemsApi.completeTask({"uuid": task.uuid, "completeDate": task.completeDate.toISOString()}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result)
                    UpdateTaskPathParent(itemsRepo._allItems, resp.result)

                    itemsRepo.pubs.task.update.notifySubscribers(resp.result)
                },
                function(reason) {
                    console.log("task.complete.failure", reason)
                }
            )
        },
        setPriority: function(task) {
            gapi.client.itemsApi.setPriority({"uuid": task.uuid, "priority": task.priority}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result)
                    UpdateTaskPathParent(itemsRepo._allItems, resp.result)

                    itemsRepo.pubs.task.update.notifySubscribers(resp.result)
                },
                function(error) {
                    msg = error.result.error.message
                    $.notify({
                    	message: `task.setPriority.failure ${msg}`
                    },{
                    	type: 'danger'
                    });
                    console.log(`task.setPriority.failure ${msg}`)
                })
        },
        getCurrent: function(callback) {
            gapi.client.itemsApi.getCurrentTaskList().execute(
                function(resp) {
                    if (!Array.isArray(resp.items)) {
                        resp.items = []
                    }
                    resp.items.forEach(convertItemRecordInplace);
                    callback(filterOldCompletedTasks(resp.items));
                },
                function(reason) {
                    console.log("task getCurrentList failure", reason)
                })
        }
    },

    item: {
        add: function(item) {
            gapi.client.itemsApi.addItem({item: item}).then(
                function(resp) {
                    convertItemRecordInplace(resp.result.item)
                    itemsRepo.pubs.item.add.notifySubscribers(resp.result.item);
                },
                function(reason) {
                    console.log("item add failure", reason)
                })
        }
    },

    any: {
        remove: function(uuid) {

        },
        move: function(uuid, newParentUuid) {

        },
        getAll: function(callback) {
            if (Array.isArray(itemsRepo._allItems)) {
                callback(itemsRepo._allItems);
            } else {
                gapi.client.itemsApi.getItems().then(
                    function(resp) {

                        var items = resp.result.variantItems
                        if (!Array.isArray(items)) {
                            items = []
                        }
                        items = convertVariantItems(items)

                        items.forEach(convertItemRecordInplace);
                        items.filter(isTask).forEach((task) => {
                            if (typeof task.priority == 'undefined') task.priority = 0
                        })
                        itemsRepo._allItems = items;

                        AddTasksPathsParents(items)

                        callback(filterOldCompletedTasks(items));
                    },
                    function(reason) {
                        console.log("any getAll failure", reason);
                    });
            }
        }
    }
}




