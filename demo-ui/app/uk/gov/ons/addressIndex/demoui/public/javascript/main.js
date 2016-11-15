$('.fa-2x').click(function() {
    $('.extra-info').toggle();
});

$(function() {
    $('#clearElasticSearchForm').click(function() {
         $('#address').val('');
         $('#street').val('');
         $('#town').val('');
         $('#postcode').val('');
    });
});

$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
});

$(function() {
    $('#clearProposeNewAddressForm').click(function() {
         $('#numberOrName').val('');
         $('#street').val('');
         $('#town').val('');
         $('#postcode').val('');
         $('#notes').val('');
         $('#email').val('');
         $('#fullName').val('');
    });
});

