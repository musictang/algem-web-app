/* French initialisation for the jQuery UI date picker plugin. */
/* Written by Keith Wood (kbwood@virginbroadband.com.au) and St&eacute;phane Nahmani (sholby@sholby.net). */
jQuery(function($){
	$.datepicker.regional['fr'] = {
    clearText: 'Effacer', clearStatus: '',
		closeText: 'Fermer', closeStatus: 'Fermer sans modifier',
		prevText: 'Pr&eacute;c', prevStatus: 'Voir le mois pr&eacute;c&eacute;dent',
		nextText: 'Suiv', nextStatus: 'Voir le mois suivant',
		currentText: 'Aujourd\'hui', currentStatus: 'Voir le mois courant',
		monthNames: ['Janvier','F&eacute;vrier','Mars','Avril','Mai','Juin',
		'Juillet','Ao&ucirc;t','Septembre','Octobre','Novembre','D&eacute;cembre'],
		monthNamesShort: ['Jan','F&eacute;v','Mar','Avr','Mai','Jun',
		'Jul','Ao&ucirc;','Sep','Oct','Nov','D&eacute;c'],
		monthStatus: 'Voir un autre mois', yearStatus: 'Voir une autre ann&eacute;e',
		weekHeader: 'Sm', weekStatus: '',
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		dayStatus: 'Utiliser DD comme premier jour de la semaine', dateStatus: 'Choisir le DD, MM d',
		dateFormat: 'dd-mm-yy', firstDay: 1,
		initStatus: 'Choisir la date', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['fr']);
});


