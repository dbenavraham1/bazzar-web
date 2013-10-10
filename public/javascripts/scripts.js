$(function(){
	$('.animated > li').hover(function(){
		$(this).find('div[class^="contr-"]').stop().slideDown('fast');
	},
	function(){
		$(this).find('div[class^="contr-"]').stop().slideUp('slow');
	});
});