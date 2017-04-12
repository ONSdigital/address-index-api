/*!
 * Sign Up/Login Box v0.0.1 (http://codepen.io/koheishingai/FLvgs)
 * Copyright 2014 Kohei Shingai.
 * Licensed under MIT
 */

$(function () {
    var flg = {};
    var passlen;
    var namelen;
    initub();

    $("#name").keyup(function () {
        var namelen = $('#name').val().length;
        if (namelen > 6 || namelen == 0) {
            $('#name').css('background', 'rgb(255, 214, 190)');
            blsp();
            if (namelen != 0) {
                $('#namealert').css('color', 'rgb(255, 57, 19)').text('ID: User Name Too long').fadeIn()
            } else {
                $('#namealert').css('color', 'rgb(255, 57, 19)').text('ID: Please enter a User Name').fadeIn()
            }
            flg.name = 0
        } else {
            $('#name').css('background', 'rgb(255, 255, 255)');
            $('#namealert').css('color', 'rgb(17, 170, 42)').text('ID: Ok').fadeIn();
            flg.name = 1;
            tcheck()
        }
    });
    $("#pass").keyup(function () {
        passlen = $('#pass').val().length;
        if (passlen > 10 || passlen == 0) {
            $('#pass').css('background', 'rgb(255, 214, 190)');
            blsp();
            if (passlen != 0) {
                $('#passal').css('color', 'rgb(255, 57, 19)').text('Password: Too long').fadeIn()
            } else {
                $('#passal').css('color', 'rgb(255, 57, 19)').text('Password: Null').fadeIn()
            }
            flg.pass = 0
        } else {
            $('#pass').css('background', 'rgb(255, 255, 255)');
            $('#passal').css('color', 'rgb(17, 170, 42)').text('Password: Ok').fadeIn();
            flg.pass = 1;
            tcheck()
        }
    });

    function tcheck() {
        if (flg.name == 1 && flg.pass == 1) {
            $('#loginbtn').css('opacity', '1').css('cursor', 'pointer')
        } else {
            blsp()
        }
    }
    $('#loginbtn').click(function () {
        if (flg.name == 1 && flg.pass == 1) {
            $('#submit_giff').fadeIn();
            $('#name, #pass, #namealert, #passal, #loginbtn').css('opacity', '0.2');
            $('#close').fadeIn()

        }
    });
    $('#close').click(function () {
        init();
        initub();
        $('#close').hide()
    });

    function initub() {
        flg.name = -1;
        flg.pass = -1;
        $('#submit_giff').hide();
        $('#namealert').hide();
        $('#passal').hide();
        $('#name').css('background', 'rgb(255, 255, 255)');
        $('#pass').css('background', 'rgb(255, 255, 255)');
        $('#name, #pass').val('')
    }

    function blsp() {
        $('#loginbtn').css('opacity', '0.2').css('cursor', 'default')
    }
});