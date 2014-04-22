$(function() {
    var urlArray = window.location.href.split("/"),
        name = urlArray[4];
    console.log(name);
    $("#name").text(name.charAt(0).toUpperCase() + name.slice(1) + "'s Profile");

    $.get("/rides/" + name, function (result) {
        rides = JSON.parse(result); 
        console.log(rides);
        $.each(rides, function(i, ride) {
            console.log(ride)
            $("#ridesTable")
                .append($("<tr></tr>")
                    .append($("<td></td>").text("4/13/14"))
                    .append($("<td></td>").text(ride.distance))
                    .append($("<td></td>").text(Math.floor(ride.time/60)+":"+(ride.time/60-Math.floor(ride.time/60))*60))
                    .append($("<td></td>").text(ride.distance/(ride.time/3600)))
                    .append($("<td></td>").text(ride.maxspeed))
            );
        });
    });
})