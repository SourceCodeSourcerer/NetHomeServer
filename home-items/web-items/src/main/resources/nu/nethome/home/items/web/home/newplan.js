/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


$(document).ready(function () {
    $(".phomeitem").hide();
    $(".panelclose").click(function () {
        $(".pitemlistpanel").fadeOut();
    });
    $(".draggable").draggable({stop: repositionItem});
    $(".poppable").click(onHover);
    $(".closebutton").click(onClose);
});

function showItemSelectionPanel() {
    $(".pitemlistpanel").fadeIn();
}

function onClose(event) {
    var closeButton = event.currentTarget;
    var elementToClose = closeButton.parentElement.parentElement.parentElement;
    $(elementToClose).fadeOut();
}

function onHover(event) {
    var evt = event.currentTarget;
    var itemId = $(event.currentTarget).attr("data-item");
    $(".phomeitem[data-item!='" + itemId + "']").fadeOut();
    var objid = $(".phomeitem[data-item=" + itemId + "]").get(0);
    //get width and height of background map
//    var mapWidth  = evt.parentNode.offsetWidth;
//    var mapHeight = evt.parentNode.offsetHeight;
//    //get width and height of the popup
//    var toopTipWidth = objid.offsetWidth;
//    var toopTipHeight = objid.offsetHeight;
//    //figure out where tooltip should be places based on point location
    var newX = evt.offsetLeft + 65;
    var newY = evt.offsetTop + 25;
//    //check if tooltip fits map width
//    if ((newX + toopTipWidth) > mapWidth) {
//        objid.style.left = newX-toopTipWidth-24 + 'px';
//    } else {
//        objid.style.left = newX + 'px';
//    };
//    //check if tooltip fits map height
//    if ((newY + toopTipHeight) > mapHeight) {
//        objid.style.top = newY-toopTipHeight-14 + 'px';
//    } else {
//        objid.style.top = newY + 'px';
//    };
    objid.style.top = newY + 'px';
    objid.style.left = newX + 'px';
    $(objid).fadeIn();
}

function callItemAction(item, action) {
    var url = homeManager.baseURL + "?a=ajax&name=" + escape(item) + "&action=" + escape(action);
    $.get(url, getItemValues);
}

function getItemValues() {
    var valueElements = $(".itemvalue").toArray();
    var parameter = "";
    var separator = "";
    var singleGuard = {};
    var id;
    var url = homeManager.baseURL + "?a=ajax&f=getdefatts&items="
    for (i = 0; i < valueElements.length; i++) {
        id = $(valueElements[i]).attr("data-item");
        if (!singleGuard[id]) {
            parameter = parameter + separator + id;
            separator = "-";
            singleGuard[id] = true;
        }
    }
    url = url + parameter;
    $.getJSON(url, updateItemValues);
}

function updateItemValues(itemMainAttributeValues) {
    var attributeValueElements = $(".itemvalue").toArray();
    var itemId;
    var i;
    var iconClassForValue;
    var lastIconClass;
    var attributeValue;

    for (i = 0; i < attributeValueElements.length; i++) {
        itemId = $(attributeValueElements[i]).attr("data-item");
        if (itemMainAttributeValues[itemId]) {
            attributeValue = itemMainAttributeValues[itemId];
            if ($(attributeValueElements[i]).attr("data-unit")) {
                attributeValue += " " + $(attributeValueElements[i]).attr("data-unit");
            }
            attributeValueElements[i].innerHTML = attributeValue;
        }
    }
    var icons = $(".icon").toArray();
    for (i = 0; i < icons.length; i++) {
        itemId = $(icons[i]).attr("data-item");
        if (itemId && itemMainAttributeValues[itemId]) {
            iconClassForValue = $(icons[i]).attr("data-" + itemMainAttributeValues[itemId]);
            lastIconClass = $(icons[i]).attr("data-lastclass");
            if (lastIconClass) {
                $(icons[i]).removeClass(lastIconClass);
                $(icons[i]).removeAttr("data-lastclass");
            }
            if (iconClassForValue) {
                $(icons[i]).addClass(iconClassForValue);
                $(icons[i]).attr("data-lastclass", iconClassForValue);
            }
        }
    }
}

function repositionItem(event, ui) {
    var theItem = event.target;
    var url = homeManager.baseURL + "?a=ajax&f=reposition&item=";
    url = url + $(theItem).attr("data-item");
    url = url + "&plan=" + $(theItem).attr("data-plan");
    url = url + "&x=" + ui.position.left;
    url = url + "&y=" + ui.position.top;
    $.get(url);
}

function gotoPlanEditPage() {
    var planId = $(".plan").attr("data-item");
    var subpage = "";
    if (homeManager.subpage) {
        subpage = "&returnsp=" + homeManager.subpage;
    }
    location.href = homeManager.baseURL + "?page=edit&mode=edit&return=plan&name=" + planId + subpage;
    return "/home?page=edit&item=" + planId;
}

function playSound(soundfile) {
    document.getElementById("dummy").innerHTML =
        "<embed src=\"" + soundfile + "\" hidden=\"true\" autostart=\"true\" loop=\"false\" />";
}


