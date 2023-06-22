//alert("This is page")
const toggleSidebar = () => {

	if ($('.sidebar').is(":visible")) {
		$(".sidebar").css("display", "none");
		//$(".content").css("margin-left","10px");
		//$(".content").css("margin-right","10px");
	} else {
		$(".sidebar").css("display", "block");
		//$(".content").css("margin-left","19%");
		//$(".content").css("margin-right","10px");
	}

};

const search = () => {

	var query = $("#search-input").val();

	if (query == '') {
		$(".search-result").hide();
	} else {

		var url = `http://localhost:9090/search/${query}`;

		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			//data......
			
			var text;
			if (data.length == 0) {
				text= `<div class='list-group'>`
				text += `<h6 class="text-center"><span>No Contact Found</span> </h6>`;
				text += `</div>`;
			}
			else {
				text = `<div class='list-group'>`

				data.forEach((contact) => {
					text += `<a class="list-group-item list-group-item-action" href='/user/${contact.cId}/contact'}><span>${contact.name}</span> </a>`
				})

				text += `</div>`;
			}
			$(".search-result").html(text);
			$(".search-result").show();
		})

	}
}