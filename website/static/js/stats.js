$(function() {
    $.get("/rides", function (result) {
        rides = JSON.parse(result).sort(function(a, b) {return b.distance - a.distance}); 
        $.each(rides, function(i, ride) {
            console.log(ride)
            $("#statstable")
                .append($("<tr></tr>")
                    .append($("<td></td>").text(ride.username.charAt(0).toUpperCase() + ride.username.slice(1)))
                    .append($("<td></td>").text("5/8/14"))
                    .append($("<td></td>").text(ride.distance))
                    .append($("<td></td>").text(Math.floor(ride.time/60)+":"+(ride.time/60-Math.floor(ride.time/60))*60))
                    .append($("<td></td>").text(ride.distance/(ride.time/3600)))
                    .append($("<td></td>").text(ride.maxspeed))
            );
        });
    });
})