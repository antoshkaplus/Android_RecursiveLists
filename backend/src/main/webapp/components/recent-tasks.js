
(function () {

    var vm = {
        recentTasks: ko.observable([])
    }

    externalApis.itemsLoaded.subscribe(function(val) {
        if (!val) return;
        itemsRepo.any.getAll(function(allItems) {
            var allTasks = allItems.filter(isTask)

            allItems = new Map(allItems.map(item => [item.uuid, item]))
            parentTasks = new Set()

            for (let task of allTasks) {
                child = task;
                while (!child.parent) {
                    parent = child.parent;
                    if (!parent) break;
                    if (isTask(parent)) parentTasks.add(parent.uuid)
                    child = parent;
                }
            }

            leafTasks = allTasks.filter(task => !parentTasks.has(task))

            leafTasks.sort(function (a, b) {
                return b.updateDate.getTime() - a.updateDate.getTime();
            })

            vm.recentTasks(leafTasks);
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

    thatDoc.registerElement('components-recent-tasks', {prototype: Element});
})()