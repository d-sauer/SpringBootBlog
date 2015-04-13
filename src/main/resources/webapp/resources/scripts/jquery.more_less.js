$(function() {
	var more_less = window.more_less;
	if (more_less === undefined) {
		window.more_less = true;
		$('.more').each(function() {
			$(this).click(function() {
				var el = $(this);
				var more = el.attr('more_content');
				var more_show_text = el.attr('more_show_text');
				var more_hide_text = el.attr('more_hide_text');
				
				var content = $('#' + more);
				if (content != undefined && content != null) {
					if (content.css('display') == 'none') {
						content.show(500);
						el.text(more_show_text);
					} else {
						content.hide(300);
						el.text(more_hide_text);
					}
				}
			});
		});
	}
});
