$(function() {
	var contPrevNotifyType = null;

	$('#contactdiv #contact_btn').click(function () {
		$('#contactdiv #notify_wrapper').hide();
		$('#ajax_loader').show(100);
		$.ajax({
			  url: "contact_send",
			  type: "POST",			  
			  data: $('#contactdiv :input').serialize(),
			  cache: false
			}).done(function(data) {
				if (data.notifyType != undefined && data.notifyType != null) {
					if (contPrevNotifyType != null)
						$('#contactdiv #notify').removeClass(contPrevNotifyType); 
					
					contPrevNotifyType = data.notifyType.toLowerCase();
					$('#contactdiv #notify').addClass(contPrevNotifyType);
					$('#contactdiv #notify').html(data.message);
					$('#contactdiv #notify_wrapper').show(200);
					
					if (data.status == 1) {
						$('#contactdiv').find('input[type=text], textarea').val('');
						setTimeout( function() { $('#contactdiv #notify_wrapper').hide(1000); }, 10000);
					}
				}
			}).always(function() {
				$('#ajax_loader').hide(100);;
			});		
	});
});