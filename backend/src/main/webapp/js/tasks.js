
// returns word starts -> task uuid map
function BuildTasksSearchMap(allItems) {
    var allTasks = allItems.filter(isTask)

    var searchMap = new Map()

    var regex = /[a-zA-Z\u{0400}-\u{04FF}\+\-]{2,}/ug;
    for (let task of allTasks) {
        for (let word of task.title.match(regex)) {
            word = word.toLowerCase()
            for (var len = 2; len <= word.length; ++len) {
                var start = word.substr(0, len)

                var startTasks = undefined
                if (typeof (startTasks = searchMap.get(start)) == 'undefined') {
                    startTasks = []
                    searchMap.set(start, startTasks)
                }
                startTasks.push(task)
            }
        }
    }

    return searchMap
}

function AddTasksPathsParents(allItems) {
    var allTasks = allItems.filter(isTask)

    allItems = new Map(allItems.map(item => [item.uuid, item]))

    for (let task of allTasks) {
        child = task;
        while (!child.parent) {
            parent = allItems.get(child.parentUuid);
            if (!parent) break;
            child.parent = parent;
            child = parent;
        }
    }

    for (let s of allTasks) {
        s.path = ""
        it = s.parent;
        while (it) {
            s.path = " / " + it.title + s.path;
            it = it.parent;
        }
        s.path += " / ";
    }
}

function UpdateTaskPathParent(allItems, updatedTask) {

    index = allItems.findIndex((elem) => { elem.uuid == updatedTask.uuid })
    allItems[index] = updatedTask

    itemsMap = new Map(allItems.map(item => [item.uuid, item]))

    parent = itemsMap.get(updatedTask.parentUuid)
    if (parent) updatedTask.parent = parent

    updatedTask.path = ""
    it = updatedTask.parent;
    while (it) {
        updatedTask.path = " / " + it.title + updatedTask.path;
        it = it.parent;
    }
    updatedTask.path += " / ";
}