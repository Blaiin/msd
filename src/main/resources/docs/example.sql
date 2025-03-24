select
    ma."AmbitoID",
    ma."AzioneID",
    ma."Classe",
    ma."ControlloID",
    ma."Destinatario",
    ma."FonteDatiID",
    ma.ORDINEAZIONE,
    ma."Programma",
    ma."SogliaID",
    ma."SQLScript",
    ma."TipoAzioneID",
    ma."TipoControlloID",
    ma."UtentiFonteDatiID"
from
    "MON_Azione" ma;