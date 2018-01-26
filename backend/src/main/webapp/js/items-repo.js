
itemsRepo = {

    pubs: {
        task: {
            add: ko.subscribable(),
            makeCurrent: ko.subscribable(),
            removeCurrent: ko.subscribable(),
            complete: ko.subscribable()
        },

        item: {
            add: ko.subscribable()
        },

        any: {
            move: ko.subscribable(),
            remove: ko.subscribable()
        }
    },

    task: {
        add: function() {
        // this has to go away.. pass ready to use object in
            title = $('#item').val()
            var item = new Item(title)
            item.kind = "Task"
            gapi.client.itemsApi.addItem({task: item}).then(
               function(resp) {
                   globalHandler.task.add.notifySubscribers(resp.result.task, "success")
        //           console.log(item, "add item success")
               },
               function(reason) {
                   globalHandler.task.add.notifySubscribers(item, "failure")
        //           console.log(reason, "add item failure")
               })
        },
        makeCurrent: function(task) {
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
        },
        removeCurrent: function(task) {
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
        },
        completeTask: function(task) {
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
    },
    item: {
        add: function() {
        // this has to go away.. pass ready to use object in

            if (isTask(viewModel.parent())) throw "Can't insert Item into Task."
            title = $('#item').val()
            var item = new Item(title)
            item.kind = "Item"
            gapi.client.itemsApi.addItem({item: item}).then(
                function(resp) {
                    globalHandler.item.add.notifySubscribers(resp.result.item, "success");
        //            console.log(item, "add item success")
                },
                function(reason) {
                    globalHandler.item.add.notifySubscribers(item);
        //            console.log(reason, "add item failure")
                })
        }
    },
    any: {
        remove: function(uuid) {

        },
        move: function(uuid, newParentUuid) {

        }
    }
}




