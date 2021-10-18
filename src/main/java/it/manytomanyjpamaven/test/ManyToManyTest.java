package it.manytomanyjpamaven.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.manytomanyjpamaven.dao.EntityManagerUtil;
import it.manytomanyjpamaven.model.Ruolo;
import it.manytomanyjpamaven.model.StatoUtente;
import it.manytomanyjpamaven.model.Utente;
import it.manytomanyjpamaven.service.MyServiceFactory;
import it.manytomanyjpamaven.service.RuoloService;
import it.manytomanyjpamaven.service.UtenteService;

public class ManyToManyTest {

	public static void main(String[] args) {
		UtenteService utenteServiceInstance = MyServiceFactory.getUtenteServiceInstance();
		RuoloService ruoloServiceInstance = MyServiceFactory.getRuoloServiceInstance();

		// ora passo alle operazioni CRUD
		try {

			// inizializzo i ruoli sul db
			initRuoli(ruoloServiceInstance);

			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testInserisciNuovoUtente(utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testCollegaUtenteARuoloEsistente(ruoloServiceInstance, utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testModificaStatoUtente(utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testDeleteRuoloNonAssociato(ruoloServiceInstance, utenteServiceInstance);
			System.out.println("In tabella Utente ci sono " + utenteServiceInstance.listAll().size() + " elementi.");

			testCercaMeseGiugno(utenteServiceInstance);

			testUtenteAdmin(utenteServiceInstance);
			
			testAdminDisabilitati(ruoloServiceInstance, utenteServiceInstance);
			
			testUtentiConPasswordLunghezzaMinoreOtto(utenteServiceInstance);
			
			testDescrizioneDistintaRuoliUtenti(utenteServiceInstance);
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// questa è necessaria per chiudere tutte le connessioni quindi rilasciare il
			// main
			EntityManagerUtil.shutdown();
		}

	}

	private static void initRuoli(RuoloService ruoloServiceInstance) throws Exception {
		if (ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN") == null) {
			ruoloServiceInstance.inserisciNuovo(new Ruolo("Administrator", "ROLE_ADMIN"));
		}

		if (ruoloServiceInstance.cercaPerDescrizioneECodice("Classic User", "ROLE_CLASSIC_USER") == null) {
			ruoloServiceInstance.inserisciNuovo(new Ruolo("Classic User", "ROLE_CLASSIC_USER"));
		}
	}

	private static void testInserisciNuovoUtente(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testInserisciNuovoUtente inizio.............");

		Utente utenteNuovo = new Utente("pippo.rossi", "xxx", "pippo", "rossi", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito ");

		System.out.println(".......testInserisciNuovoUtente fine: PASSED.............");
	}

	private static void testCollegaUtenteARuoloEsistente(RuoloService ruoloServiceInstance,
			UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testCollegaUtenteARuoloEsistente inizio.............");

		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.caricaSingoloElemento(1L);
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testCollegaUtenteARuoloEsistente fallito: ruolo inesistente ");

		// mi creo un utente inserendolo direttamente su db
		Utente utenteNuovo = new Utente("mario.bianchi", "JJJ", "mario", "bianchi", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testInserisciNuovoUtente fallito: utente non inserito ");

		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);
		// per fare il test ricarico interamente l'oggetto e la relazione
		Utente utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(utenteNuovo.getId());
		if (utenteReloaded.getRuoli().size() != 1)
			throw new RuntimeException("testInserisciNuovoUtente fallito: ruoli non aggiunti ");

		System.out.println(".......testCollegaUtenteARuoloEsistente fine: PASSED.............");
	}

	private static void testModificaStatoUtente(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testModificaStatoUtente inizio.............");

		// mi creo un utente inserendolo direttamente su db
		Utente utenteNuovo = new Utente("mario1.bianchi1", "JJJ", "mario1", "bianchi1", new Date());
		utenteServiceInstance.inserisciNuovo(utenteNuovo);
		if (utenteNuovo.getId() == null)
			throw new RuntimeException("testModificaStatoUtente fallito: utente non inserito ");

		// proviamo a passarlo nello stato ATTIVO ma salviamoci il vecchio stato
		StatoUtente vecchioStato = utenteNuovo.getStato();
		utenteNuovo.setStato(StatoUtente.ATTIVO);
		utenteServiceInstance.aggiorna(utenteNuovo);

		if (utenteNuovo.getStato().equals(vecchioStato))
			throw new RuntimeException("testModificaStatoUtente fallito: modifica non avvenuta correttamente ");

		System.out.println(".......testModificaStatoUtente fine: PASSED.............");
	}

	public static void testDeleteRuoloNonAssociato(RuoloService ruoloServiceInstance,
			UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testDeleteRuoloNonAssociato inizio.............");

		Ruolo testNuovoRuolo = new Ruolo("Administrator", "random_role");

		Utente testNuovoUtente = new Utente("leos", "ffw01", "leonardo", "iappelli", new Date());

		ruoloServiceInstance.inserisciNuovo(testNuovoRuolo);

		utenteServiceInstance.inserisciNuovo(testNuovoUtente);

		if (testNuovoRuolo.getId() == null && testNuovoUtente.getId() == null) {
			throw new RuntimeException("Non è stato possibile aggiungere utente  e ruolo ");
		}

		utenteServiceInstance.aggiungiRuolo(testNuovoUtente, testNuovoRuolo);

		Utente utenteReloaded = utenteServiceInstance.caricaUtenteSingoloConRuoli(testNuovoUtente.getId());

		if (utenteReloaded.getRuoli().size() != 1)
			throw new RuntimeException("testInserisciNuovoUtente fallito: ruoli non aggiunti ");

		// per eliminare subito il ruolo non associato
		// ruoloServiceInstance.rimuovi(testNuovoRuolo);

		System.out.println(".......testDeleteRuoloNonAssociato finito.............");

	}

	public static void testCercaMeseGiugno(UtenteService utenteServiceInstance) throws Exception {

		System.out.println(".......testCercaMeseGiugno inizio.............");

		String dataDaControllare = "2021-06-04";
		Date dataCreatedNum = new SimpleDateFormat("yyyyy-MM-dd").parse(dataDaControllare);

		Utente nuovoUtente = new Utente("cia", "slls82aas", "ciao", "romano", dataCreatedNum);

		utenteServiceInstance.inserisciNuovo(nuovoUtente);

		if (nuovoUtente.getId() == null) {
			throw new RuntimeException("Non è stato possibile aggiungere un nuovo utente!");
		}

		System.out.println(utenteServiceInstance.cercaTuttiUtentiDaGiugnio().size());

		System.out.println(".......testCercaMeseGiugno finito.............");
	}

	public static void testUtenteAdmin(UtenteService utenteServiceInstance) throws Exception {

		System.out.println(".......testUtenteAdmin inizio.............");

		System.out.println(utenteServiceInstance.cercaUtentiAdmin());

		System.out.println(".......testUtenteAdmin finito.............");
	}

	public static void testDescrizioneDistintaRuoliUtenti(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testDescrizioneDistintaRuoliUtenti inizio.............");

		System.out.println(utenteServiceInstance.cercaDescrizioneRuoliConUtenti().size());

		System.out.println(".......testDescrizioneDistintaRuoliUtenti finito.............");
	}
	
	public static void testAdminDisabilitati(RuoloService ruoloServiceInstance, UtenteService utenteServiceInstance)
			throws Exception {
		System.out.println(".......testAdminDisabilitati inizio.............");

		Ruolo ruoloEsistenteSuDb = ruoloServiceInstance.cercaPerDescrizioneECodice("Administrator", "ROLE_ADMIN");
		if (ruoloEsistenteSuDb == null)
			throw new RuntimeException("testRimuoviRuoloDaUtente fallito: ruolo inesistente ");

		Utente utenteNuovo = new Utente("s.p", "adas@2", "gas", "iabf", new Date());

		utenteNuovo.setStato(StatoUtente.DISABILITATO);

		utenteServiceInstance.inserisciNuovo(utenteNuovo);

		utenteServiceInstance.aggiungiRuolo(utenteNuovo, ruoloEsistenteSuDb);

		System.out.println(utenteServiceInstance.adminDisabilitati());

		System.out.println(".......testAdminDisabilitati fine: PASSED.............");

	}
	
	public static void testUtentiConPasswordLunghezzaMinoreOtto(UtenteService utenteServiceInstance) throws Exception {
		System.out.println(".......testUtentiConPasswordLunghezzaMinoreOtto inizio.............");

		System.out.println(utenteServiceInstance.cercaUtentiConPassword().size());

		System.out.println(".......testUtentiConPasswordLunghezzaMinoreOtto finito.............");
	}
	
}
