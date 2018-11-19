
(function () {

    var thatDoc = document;
    var thisDoc = document.currentScript.ownerDocument

    var tmpl = thisDoc.querySelector('template')

    function EditTaskViewModel(params, componentInfo) {
        var vm = this

        vm.componentUuid = guid()

        $(componentInfo.element).find('.modal').attr('id', vm.componentUuid)

        vm.editedTask = ko.observable()
        vm.editedTitle = ko.observable()
        vm.editedPriority = ko.observable()
        vm.makeItem = ko.observable()

        params.editedTask.subscribe((task) => {
            if (!task) return
            this.editedTask(task)
            this.editedTitle(task.title)
            this.editedPriority(task.priority)
            this.makeItem(false)
            $('#' + this.componentUuid).modal()
        })

        vm.save = () => {
            $('#' + this.componentUuid).modal('hide')

            newPriorityStr = vm.editedPriority()
            if (/^\d+$/.test(newPriorityStr) == false)
            {
                $.notify({message: `priority has to be a integer: ${newPriorityStr}`},
                         {type: 'danger'});
                return
            }
            newPriority = parseInt(newPriorityStr)
            if (newPriority < 0) {
                $.notify({message: `priority has to be a positive integer: ${newPriority}`},
                         {type: 'danger'});
                return
            }
            this.editedTask().priority = newPriority
            itemsRepo.task.setPriority(this.editedTask())
        }
    }

    ko.components.register('ko-component-edit-task', {
        viewModel: {
            createViewModel: function(params, componentInfo){
                return new EditTaskViewModel(params, componentInfo)
            }
        },
        template: tmpl.innerHTML
    })

})()