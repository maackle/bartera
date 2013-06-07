
$ ->

  class UploadForm

    lastIndex: 0

    constructor: (@$container) ->
      @$gallery = @$container.find('.gallery')

    initialize: ->
      fu = this.$container.find('input[type=file]').fileupload
        url: "/haves/saveImage"
        dataType: "json"
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
        disableImageResize: false
        previewMaxWidth: 100
        previewMaxHeight: 100
        previewCrop: true

      fu.on 'fileuploadadd', (e, data) =>
        for file, index in data.files
          this.$gallery.append """
                               <div class="preview unprocessed" data-index=#{index}>
                               </div>
                               """

      fu.on 'fileuploadprocessalways', (e, data) =>
        console.log 'processalways', data
        for file, index in data.files
          $div = this.$gallery.find(".unprocessed[data-index=#{index}]")
          $div.removeClass('unprocessed')

          if file.preview
            $div.append(file.preview)





  addHaveForm = new UploadForm($('.have-add-form'))
  addHaveForm.initialize()





##################################################################################################


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

