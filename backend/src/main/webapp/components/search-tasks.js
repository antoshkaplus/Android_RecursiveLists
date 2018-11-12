
(function () {

    var vm = {
        searchResult: ko.observable([]),
        searchRequest: ko.observable(),

        searchMap: new Map(),
        allTasks: new Map()
    }

    vm.searchRequest.subscribe((searchString) => {
        res = vm.searchMap.get(searchString.toLowerCase())
        if (res == undefined) vm.searchResult([])

        vm.searchResult(res)
    })

    externalApis.itemsLoaded.subscribe(function(val) {
        if (!val) return;
        itemsRepo.any.getAll(function(allItems) {

            vm.searchMap = BuildTasksSearchMap(allItems)

            var allTasks = allItems.filter(isTask)
            vm.allTasks = new Map(allTasks.map(task => [task.uuid, task]))
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

    thatDoc.registerElement('components-search-tasks', {prototype: Element});
})()