
// now we need ability to go back.
// either keep tree in javascript of where we going each time. or do requests.
// lets do online.
// so we have our parent guy. we need to get his parent and it's children


//server:client_id:
var CLIENT_ID = "582892993246-g35aia2vqj3dl9umucp57utfvmvt57u3.apps.googleusercontent.com"
var SCOPES = "https://www.googleapis.com/auth/userinfo.email"
var API_KEY = "AIzaSyD81UMLvDPOz_gOov_9fuaWZopCNwWrS-4"

$(function() {
    viewModel = {
        itemList: ko.observable([]),
        parentUuid: ko.observable(""),
        parentUuidPath: [],

    }
    viewModel.itemKindClass = ko.pureComputed(function(kind) {
        return kind == "Task" ? "task" : "item";
    }, viewModel);

    viewModel.parentUuid.subscribe(fillItemList)
    viewModel.parentUuid.subscribe(function (val) {
        //if (!val) return

    }, null, "beforeChange")

    ko.applyBindings(viewModel)
})


function itemKindClass(kind) {
    return kind == "Task" ? "task" : "item";
}

function isTask(kind) {
    return kind == "Task"
}

function completeTask(task) {
    task.completeDate = new Date()
    gapi.client.itemsApi.addTaskOnline(task).execute()
}

function back() {
    if (viewModel.parentUuidPath.length == 0) return;
    uuid = viewModel.parentUuidPath.pop()
    viewModel.parentUuid(uuid)
}

function onItemClick(item) {
    viewModel.parentUuidPath.push(viewModel.parentUuid())
    viewModel.parentUuid(item.uuid)
}

function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

// Get authorization from the user to access profile info
function initAuth() {
    gapi.client.setApiKey(API_KEY);
    gapi.auth2.init({
        client_id: CLIENT_ID,
        scope: SCOPES,
    }).then(function () {
        console.log("everything is good")
        auth2 = gapi.auth2.getAuthInstance();
        auth2.isSignedIn.listen(updateSigninStatus);
        updateSigninStatus(auth2.isSignedIn.get());
        $('#login').click(auth)
    });
}

function updateSigninStatus(isSignedIn) {
    if (isSignedIn) {
        console.log(auth2.currentUser.get().getBasicProfile().getGivenName());
        loadApi();
    } else {
        console.log("terrible")
    }

}

function auth() {
    auth2.signIn();
}

// this is where we starting out
function init() {
    gapi.load('client:auth2', initAuth);
}

function loadApi() {
    var apiName = 'itemsApi';
    var apiVersion = 'v2';
    var apiRoot = 'https://' + window.location.host + '/_ah/api';
    if (window.location.hostname == 'localhost'
      || window.location.hostname == '127.0.0.1'
      || ((window.location.port != "") && (window.location.port > 1023))) {
        // We're probably running against the DevAppServer
      apiRoot = 'http://' + window.location.host + '/_ah/api';
    }
    //apiRoot = "https://antoshkaplus-words.appspot.com/_ah/api"


    gapi.client.load(apiName, apiVersion, undefined, apiRoot).then(
        function(response) {
            gapi.client.itemsApi.getRootUuid().execute(function(resp) {
                if (resp.error != null) {
                    console.log("error getRootUuid", resp)
                    return
                }
                console.log(resp.uuid)
                viewModel.parentUuid(resp.uuid)
                console.log("root uuid set")
            })
            console.log("api loaded")
        },
        function(reason) {
            console.log("api load failure", reason)
        })


}

function Item(title) {
    this.title = title
    this.createDate = new Date()
    this.kind = "Item"
    this.uuid = guid()
    this.parentUuid = viewModel.parentUuid()
}

function addTask() {
    title = $('#item').val()
    item = new Item(title)
    item.kind = "Task"
    gapi.client.itemsApi.addTaskOnline(item).execute()
}

function addItem() {
    title = $('#item').val()
    item = new Item(title)
    item.kind = "Item"
    gapi.client.itemsApi.addItemOnline(item).execute()
}

function fillItemList() {
    gapi.client.itemsApi.getChildrenItems({parentUuid: viewModel.parentUuid()}).execute(function(resp) {
        if (resp.error != null) {
            // need to show some kind of sign to reload browser window
            // later on may try to reload by myself
            console.log("error happened", resp)
            return
        }
        // empty items with such parent
        if (!resp) resp.items = []

        viewModel.itemList(resp.items)
        console.log(resp)
    })
}