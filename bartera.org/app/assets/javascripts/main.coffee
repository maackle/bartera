
$ ->

	$('a.ajax-post').not('.ajax-waiting').click (ev) ->
		ev.preventDefault()
		req = $.ajax (
			type: "POST"
			url: $(this).attr('href')
			data: "{}"
			dataType: 'json'
			contentType: 'application/json'
		)
		.done (res) =>
			$(this).removeClass('ajax-waiting')
			$(this).parent('.ajax-toggle-pair').find('.ajax-toggle').toggleClass('ajax-toggle-active')
			console.log $(this).parent('.ajax-toggle-pair').find('.ajax-toggle')
		.fail =>
			$(this).addClass('ajax-failed')

		$(this).addClass('ajax-waiting')

		false
