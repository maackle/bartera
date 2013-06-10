
$ ->

  class UploadForm

    constructor: (@$container, @have_id) ->
      @$gallery = @$container.find('.gallery')
      @$fileinput = -> this.$container.find('input[type=file]')

    initialize: ->
      fu = @$fileinput().fileupload
        url: "/haves/#{@have_id}/saveImage"
        dataType: "json"
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
        disableImageResize: false
        previewMaxWidth: 100
        previewMaxHeight: 100
        previewCrop: true
        dropZone: @$container.find('.dropzone')

#      fu.on 'fileuploadadd', (e, data) =>
#        for file in data.files
#          console.dir file
#          this.$gallery.append """
#                               <div class="preview unprocessed" data-index=#{index}>
#                               </div>
#                               """

      fu.on 'fileuploadprocessalways', (e, data) =>
        console.log 'processalways', data
        for file, index in data.files
          $preview = $("""
                     <div class="preview">
                     </div>
                     """)
          this.$gallery.append $preview

          if file.preview
            $preview.append(file.preview)

      $('.preview.adder').click (e) =>
        @$fileinput().trigger 'click'

      $(document).bind 'drop dragover', (e) ->
        e.preventDefault()

  for form in $('.have-edit-form')
    console.log form
    addHaveForm = new UploadForm($(form), $(form).attr('data-object-id'))
    addHaveForm.initialize()

  $(document).bind 'dragover', (e) ->
    dropZone = $('.dropzone')
    timeout = window.dropZoneTimeout;
    if (!timeout)
      dropZone.addClass('in')
    else
      clearTimeout(timeout)

    found = false
    node = e.target
    foo = ->
      if (node == dropZone[0])
        found = true
      node = node.parentNode

    foo()
    foo() while node is not null

    if (found)
      dropZone.addClass('hover')
    else
      dropZone.removeClass('hover')

    window.dropZoneTimeout = setTimeout ->
      window.dropZoneTimeout = null
      dropZone.removeClass('in hover')
    , 150



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

