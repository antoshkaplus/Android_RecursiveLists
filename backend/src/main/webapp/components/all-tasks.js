
(function () {

    vm = {
        allTasks: ko.observable([])
    }

    externalApis.itemsLoaded.subscribe(function(val) {
        if (!val) return;
        itemsRepo.any.getAll(function(allItems) {
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
                s.path = "/ ";
                it = s.parent;
                while (it) {
                    s.path += it.title + " / ";
                    it = it.parent;
                }
            }

            allTasks.sort(function (a, b) {
                return (a.path + a.title).localeCompare(b.path + b.path);
            })

            vm.allTasks(allTasks);
        })
    })
//    itemsRepo.pubs.task.add.subscribe(function(newTask) {
//        if (newTask.current) {
//            vm.allTasks.unshift(newTask);
//        }
//    });
//    itemsRepo.pubs.task.update.subscribe(function(task) {
//        listTask = vm.currentTasks().find(item => item.uuid == task.uuid);
//        listTask.
//        if (listTask) {
//            vm.allTasks.replace(listTask, task)
//        }
//    }

    var thatDoc = document;
    var thisDoc = document.currentScript.ownerDocument

    var tmpl = thisDoc.querySelector('template')
    var Element = Object.create(HTMLElement.prototype)

    var shadowRoot;

    Element.createdCallback = function () {
        var clone = thatDoc.importNode(tmpl.content, true);
        this.appendChild(clone);

        ko.applyBindings(vm, this)
    }

    thatDoc.registerElement('components-all-tasks', {prototype: Element});
})()