
//server:client_id:
var CLIENT_ID = "582892993246-g35aia2vqj3dl9umucp57utfvmvt57u3.apps.googleusercontent.com"
var SCOPES = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/tasks"


function init() {
    gapi.load('client:auth2', initAuth);
}


// Get authorization from the user to access profile info
function initAuth() {
    gapi.auth2.init({
        client_id: CLIENT_ID,
        scope: SCOPES,
    }).then(
        function () {
            console.log("authorized")
            auth2 = gapi.auth2.getAuthInstance();
            auth2.isSignedIn.listen(updateSigninStatus);
            updateSigninStatus(auth2.isSignedIn.get());
        },
        function () {
            console.log("authorization failure")
        }
    );
}

function updateSigninStatus(isSignedIn) {
    if (isSignedIn) {
        console.log("signed in: ", auth2.currentUser.get().getBasicProfile().getGivenName());
        loadApi();
    } else {
        console.log("signed out")
    }
}

function auth() {
    auth2.signIn();
}

function refreshAuth() {
    gapi.auth2.getAuthInstance().currentUser.get().reloadAuthResponse().then(
        function(resp) {
            console.log("refresh auth success", resp)
        },
        function(reason) {
            console.log("refresh auth failure", reason)
        }
    )
}

function loadApi() {
    var apiName = 'itemsApi';
    var apiVersion = 'v5';
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
            viewModel.itemsApiLoaded(true)
            console.log("items api loaded")
        },
        function(reason) {
            console.log("items api load failure", reason)
        })
    gapi.client.load('tasks', 'v1', function(resp) { viewModel.gtaskApiLoaded(true); });
}