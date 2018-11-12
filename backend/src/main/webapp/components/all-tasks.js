
(function () {

    var vm = {
        allTasks: ko.observableArray([]),
        editedTask: ko.observable(),

        editTask: (task) => {
            vm.editedTask(task)
        },
        saveEdit: (changes) => {
            console.log(changes)
        }
    }

    externalApis.itemsLoaded.subscribe(function(val) {
        if (!val) return;
        itemsRepo.any.getAll(function(allItems) {
            var allTasks = allItems.filter(isTask)

            allTasks.sort(function (a, b) {
                //return (a.path + a.title).localeCompare(b.path + b.path);
                return a.priority - b.priority;
            })

            vm.allTasks(allTasks);
        })
    })
//    itemsRepo.pubs.task.add.subscribe(function(newTask) {
//        if (newTask.current) {
//            vm.allTasks.unshift(newTask);
//        }
//    });

    // TODO could be that became an Item, naybe need a flag to tell what happened
    itemsRepo.pubs.task.update.subscribe(function(task) {
        listTask = vm.allTasks().find(item => item.uuid == task.uuid);
        if (listTask) {
            vm.allTasks.replace(listTask, task)
        } else {
            vm.allTasks.unshift(task)
        }
        vm.allTasks.sort(function (a, b) {
            //return (a.path + a.title).localeCompare(b.path + b.path);
            return a.priority - b.priority;
        })
    })

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