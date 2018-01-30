
(function () {

    var vm = {
        currentTasks: ko.observableArray([])
    }

    externalApis.itemsLoaded.subscribe(function(val) {
        if (!val) return;
        itemsRepo.task.getCurrent(function(taskList) {
            vm.currentTasks(taskList)
        })
    })
    itemsRepo.pubs.task.add.subscribe(function(newTask) {
        if (newTask.current) {
            vm.currentTasks.unshift(newTask);
        }
    });
    itemsRepo.pubs.task.update.subscribe(function(task) {
        listTask = vm.currentTasks().find(item => item.uuid == task.uuid);
        if (listTask) {
            if (!task.current) {
                vm.currentTasks.remove(listTask);
                return;
            }
            vm.currentTasks.replace(listTask, task)
        } else if (task.current) {
            vm.currentTasks.unshift(task)
        }
    });


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

    thatDoc.registerElement('components-current-tasks', {prototype: Element});
})()