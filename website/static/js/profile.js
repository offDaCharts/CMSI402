$(function() {
    var urlArray = window.location.href.split("/"),
        name = urlArray[4];
    console.log(name);
    $("#name").text(name.charAt(0).toUpperCase() + name.slice(1));

    $.get("/rides/" + name, function (rides) {
        console.log(rides);
    });
})