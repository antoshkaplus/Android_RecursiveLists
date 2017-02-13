


function Gtask() {
}

// callback takes list name in.
// item: title, id, updated
Gtask.forEachList = function(reqOptions, callback, errorHandler) {
    gapi.client.tasks.tasklists.list(reqOptions).then(function(resp) {
        if (resp.code) {
            if (errorHandler) {
                errorHandler(resp.error)
            }
            return
        }

        var tasklists = resp.result.items;
        tasklists.forEach(function(item) {
            callback(item)
        })
    });
}

// callback may be called many times, takes in list of tasks
// will call empty at the end
Gtask.forTasks = function(reqOptions, callback, errorHandler) {
    gapi.client.tasks.tasks.list(reqOptions).then(function handler(resp) {
        if (resp.code) {
            if (errorHandler) {
                errorHandler(resp.error)
            }
            return
        }

        tasks = resp.result.items
        if (tasks) {
            callback(tasks)
        }

        token = resp.result.nextPageToken
        if (token) {
            options = Object.assign({pageToken: token}, reqOptions)
            gapi.client.tasks.tasks.list(options).then(handler)
        } else {
            callback(null)
        }
    })
}