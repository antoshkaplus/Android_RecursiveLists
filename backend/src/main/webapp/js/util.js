
function timestampToSend() {
    t = new Date();
    t.setSeconds(t.getSeconds() - 10);
    return t;
}