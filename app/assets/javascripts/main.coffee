
$ ->

  class UploadForm

    constructor: (@$container, object_type, @item_id) ->
      @uri_base = "/#{object_type}/#{@item_id}"
      @$gallery = @$container.find('.gallery')
      @$fileinput = -> this.$container.find('input[type=file]')

    initialize: ->
      fu = @$fileinput().fileupload
        url: "#{ @uri_base }/image"
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
        for file, index in data.files
          $preview = $("""
            <div class="thumb preview">
            </div>
                     """)
          this.$gallery.append $preview

          if file.preview
            $preview.append(file.preview)

          @update()

      fu.on 'fileuploaddone', (e, data) =>
        console.log data

      @$container.find('.adder').click (e) =>
        @$fileinput().trigger 'click'

      @$container.find('.controls .delete').click (e) =>
        $thumb = $(e.target).closest('.thumb')
        img_id = $thumb.attr('data-id')
        x = $.ajax
          url: "#{ @uri_base }/image/#{ img_id }"
          type: 'delete'

        x.done (data) =>
          $thumb.fadeOut (el)=>
            $thumb.remove()
            @update()


      $(document).bind 'drop dragover', (e) ->
        e.preventDefault()


    update: ->
      $thumbs = $('.gallery .thumb')
      console.log $thumbs.length
      if $thumbs.length > 0
        @$container.find('.empty-notice').hide()
      else
        @$container.find('.empty-notice').show()


  for form in $('.item-edit-form')
    addHaveForm = new UploadForm($(form), $(form).attr('data-object-type'), $(form).attr('data-object-id'))
    addHaveForm.initialize()

  $('.search-view-options button').on 'click', (e)->
    type = $(this).attr('data-view')
    if type == 'list'
      $('.search-results-container').removeClass('show-grid').addClass('show-list')
    else
      $('.search-results-container').removeClass('show-list').addClass('show-grid')


#  $(document).bind 'dragover', (e) ->
#    dropZone = $('.dropzone')
#    timeout = window.dropZoneTimeout;
#    if (!timeout)
#      dropZone.addClass('in')
#    else
#      clearTimeout(timeout)
#
#    found = false
#    node = e.target
#    foo = ->
#      if (node == dropZone[0])
#        found = true
#      node = node.parentNode
#
#    foo()
#    foo() while node is not null
#
#    if (found)
#      dropZone.addClass('hover')
#    else
#      dropZone.removeClass('hover')
#
#    window.dropZoneTimeout = setTimeout ->
#      window.dropZoneTimeout = null
#      dropZone.removeClass('in hover')
#    , 150
#


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

