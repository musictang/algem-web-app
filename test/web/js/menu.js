function setMenuEvents() {
  $('#help').click(function() {
    $(this).css('cursor', 'pointer');
    $('#help-content').toggle();
  });
   $('#help-close').click(function() {
    $(this).css('cursor', 'pointer');
    $('#help-content').hide();
  });

  $('#tel').click(function() {
    $(this).css('cursor', 'pointer');
    $('#tel-content').toggle();
  });
  $('#tel-close').click(function() {
    $(this).css('cursor', 'pointer');
    $('#tel-content').hide();
  });

};
