DECLARE
	ambitoID NUMBER;
	tipoControlloID NUMBER;
	tipoAzioneID NUMBER;
	controlloID NUMBER;
	fonteDatiID NUMBER;
	utentiFonteDatiID NUMBER;
	configurazioneID NUMBER;
	sogliaUnoID NUMBER;
	sogliaDueID NUMBER;
	sogliaTreID NUMBER;

BEGIN
	INSERT INTO SESAMO."MON_Ambito" ("AmbitoID", "Nome", "Destinazione")
	VALUES (1, 'Test', 'Destinazione Testing')
	RETURNING "AmbitoID" INTO ambitoID;

	INSERT INTO SESAMO."MON_TipoControllo" ("TipoControlloID", "Descrizione")
	VALUES (1, 'Test Tipo Controllo')
	RETURNING "TipoControlloID" INTO tipoControlloID;

	INSERT INTO SESAMO."MON_TipoAzione" ("TipoAzioneID", "Descrizione")
	VALUES (1, 'Test Tipo Azione')
	RETURNING "TipoAzioneID" INTO tipoAzioneID;

	INSERT INTO SESAMO."MON_Controllo" ("ControlloID", "Descrizione", "TipoControlloID", "AmbitoID", ORDINECONTROLLO)
	VALUES (1, 'Controllo Testing', tipoControlloID, ambitoID, 1)
	RETURNING "ControlloID" INTO controlloID;

	INSERT INTO SESAMO."MON_FonteDati" ("FonteDatiID", "Descrizione", "NomeDriver", URL)
	VALUES (1, 'Fonte dati per connessione di test ad ORACLE', 'oracle.jdbc.OracleDriver', 'jdbc:oracle:thin:@26.2.63.55:1521:SVU1')
	RETURNING "FonteDatiID" INTO fonteDatiID;

	INSERT INTO SESAMO."MON_SicurezzaFonteDati" ("UtentiFonteDatiID", "Descrizione", "UserID", "Password")
	VALUES (1, 'Credenziali per connessione di test ad ORACLE', 'SESAMO', 'SESAMO')
	RETURNING "UtentiFonteDatiID" INTO utentiFonteDatiID;

	INSERT INTO SESAMO."MON_Configurazione" ("ConfigurazioneID", "Nome", "SQLScript", "Schedulazione", "ControlloID", "TipoControlloID",
										"AmbitoID" , "FonteDatiID", "UtentiFonteDatiID", ORDINECONFIGURAZIONE)
	VALUES (1, 'Configurazione test, table in utilizzo -> MON_TEST',
			'SELECT COUNT(*) FROM SESAMO.MON_TEST WHERE USATA = 0;', '*/30 * * * * ?',
			controlloID, tipoControlloID, ambitoID, fonteDatiID, utentiFonteDatiID, 1)
	RETURNING "ConfigurazioneID" INTO configurazioneID;

	INSERT INTO SESAMO."MON_Soglia" ("SogliaID", "SogliaInferiore", "SogliaSuperiore", "ConfigurazioneID")
	VALUES (1, 1, 10, configurazioneID)
	RETURNING "SogliaID" INTO sogliaUnoID;

	INSERT INTO SESAMO."MON_Soglia" ("SogliaID", "SogliaInferiore", "SogliaSuperiore", "ConfigurazioneID")
	VALUES (2, 5, 20, configurazioneID)
	RETURNING "SogliaID" INTO sogliaDueID;

	INSERT INTO SESAMO."MON_Soglia" ("SogliaID", "SogliaInferiore", "SogliaSuperiore", "ConfigurazioneID")
	VALUES (3, 44, 54, configurazioneID)
	RETURNING "SogliaID" INTO sogliaTreID;

	INSERT INTO SESAMO."MON_Azione" ("AzioneID", "Destinatario", "SQLScript", "TipoAzioneID", "SogliaID", "ControlloID",
									"TipoControlloID", "AmbitoID", "FonteDatiID", "UtentiFonteDatiID", ORDINEAZIONE)
	VALUES (1, 'example.test@com.example.it', 'SELECT COUNT(*) FROM SESAMO.MON_TEST;', tipoAzioneID,
			sogliaUnoID, controlloID, tipoControlloID, ambitoID, fonteDatiID, utentiFonteDatiID, 1);

	INSERT INTO SESAMO."MON_Azione" ("AzioneID", "Destinatario", "SQLScript", "TipoAzioneID", "SogliaID", "ControlloID",
								"TipoControlloID", "AmbitoID", "FonteDatiID", "UtentiFonteDatiID", ORDINEAZIONE)
	VALUES (2, 'example.test@com.example.it', 'SELECT COUNT(*) FROM SESAMO.MON_TEST WHERE CILINDRATA = "1400cc";', tipoAzioneID,
			sogliaDueID, controlloID, tipoControlloID, ambitoID, fonteDatiID, utentiFonteDatiID, 2);

	INSERT INTO SESAMO."MON_Azione" ("AzioneID", "Destinatario", "SQLScript", "TipoAzioneID", "SogliaID", "ControlloID",
								"TipoControlloID", "AmbitoID", "FonteDatiID", "UtentiFonteDatiID", ORDINEAZIONE)
	VALUES (3, 'example.test@com.example.it', 'SELECT COUNT(*) FROM SESAMO.MON_TEST WHERE CILINDRATA = "1200cc";', tipoAzioneID,
			sogliaTreID, controlloID, tipoControlloID, ambitoID, fonteDatiID, utentiFonteDatiID, 3);
	COMMIT;

EXCEPTION
  WHEN OTHERS THEN
        ROLLBACK;

END;