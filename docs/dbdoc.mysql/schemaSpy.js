// table-based pages are expected to set 'table' to their name
var table = null;

// sync target's visibility with the state of checkbox
function sync(cb, target) {
  var checked = cb.attr('checked');
  var displayed = target.css('display') != 'none';
  if (checked != displayed) {
    if (checked)
      target.show();
    else
      target.hide();
  }
}

// sync target's visibility with the inverse of the state of checkbox
function unsync(cb, target) {
  var checked = cb.attr('checked');
  var displayed = target.css('display') != 'none';
  if (checked == displayed) {
    if (checked)
      target.hide();
    else
      target.show();
  }
}

// associate the state of checkbox with the visibility of target
function associate(cb, target) {
  sync(cb, target);
  cb.click(function() {
    sync(cb, target);
  });
}

// select the appropriate image based on the options selected
function syncImage() {
  var implied   = $('#implied').attr('checked');

  $('.diagram').hide();

  if (table) {
    if (implied && $('#impliedTwoDegreesImg').size() > 0) {
      $('#impliedTwoDegreesImg').show();
    } else {
      var oneDegree = $('#oneDegree').attr('checked');

      if (oneDegree || $('#twoDegreesImg').size() == 0) {
        $('#oneDegreeImg').show();
      } else {
        $('#twoDegreesImg').show();
      }
    }
  } else {
    var showNonKeys = $('#showNonKeys').attr('checked');

    if (implied) {
      if (showNonKeys && $('#impliedLargeImg').size() > 0) {
        $('#impliedLargeImg').show();
      } else if ($('#impliedCompactImg').size() > 0) {
        $('#impliedCompactImg').show();
      } else {
        $('#realCompactImg').show();
      }
    } else {
      if (showNonKeys && $('#realLargeImg').size() > 0) {
        $('#realLargeImg').show();
      } else {
        $('#realCompactImg').show();
      }
    }
  }
}

// our 'ready' handler makes the page consistent
$(function(){
  associate($('#implied'),         $('.impliedRelationship'));
  associate($('#showComments'),    $('.comment'));
  associate($('#showLegend'),      $('.legend'));
  associate($('#showRelatedCols'), $('.relatedKey'));
  associate($('#showConstNames'),  $('.constraint'));

  syncImage();
  $('#implied,#oneDegree,#twoDegrees,#showNonKeys').click(function() {
    syncImage();
  });

  unsync($('#implied'), $('.degrees'));
  $('#implied').click(function() {
    unsync($('#implied'), $('.degrees'));
  });

  unsync($('#removeImpliedOrphans'), $('.impliedNotOrphan'));
  $('#removeImpliedOrphans').click(function() {
    unsync($('#removeImpliedOrphans'), $('.impliedNotOrphan'));
  });
});
