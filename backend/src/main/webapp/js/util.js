
function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

function timestampToSend() {
    t = new Date();
    t.setSeconds(t.getSeconds() - 10);
    return t;
}

function Item(title, parentUuid) {
    this.title = title
    this.createDate = timestampToSend()
    this.kind = "Item"
    this.uuid = guid()
    this.parentUuid = parentUuid
}

function convertVarItem(x) {
    return x.item ? x.item : x.task;
}

function convertVariantItems(variantItems) {
    return variantItems.map(convertVarItem);
}

function convertItemRecordInplace(x) {
    ['createDate', 'updateDate', 'completeDate'].forEach(function(p) {
        if (x[p] && typeof x[p] === 'string') {
            x[p] = new Date(x[p]);
        }
    })
}

function toVariantItems(items) {
    return items.map(function(x) { return isTask(x) ? {task: x} : {item: x} })
}

function itemKindClass(kind) {
    return kind == "Task" ? "task" : "item";
}

function isTask(t) {
    return t.kind == "Task"
}
