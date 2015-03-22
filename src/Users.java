import java.util.HashSet;

/**
 * 
 * @author Flegyas
 *
 */
public class Users extends HashSet<User> {
	private static final long serialVersionUID = 1L;

	private static Users instance;

	public static Users getLocalInstance() {
		return instance == null ? instance = readLocalInstance() : instance;
	}

	private static Users readLocalInstance() {
		/*
		 * if (Constants.USERS_FILE.exists()) try { return new Gson().fromJson(
		 * new FileReader(Constants.USERS_FILE), Users.class); } catch
		 * (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
		 * e.printStackTrace(); }
		 */

		return null;
	}

	public boolean save() {
		/*
		 * FileWriter writer = null; try { writer = new
		 * FileWriter(Constants.USERS_FILE); writer.write(new
		 * GsonBuilder().setPrettyPrinting().create() .toJson(this)); } catch
		 * (IOException exc) { exc.printStackTrace(); return false; } finally {
		 * try { writer.flush(); writer.close(); } catch (IOException |
		 * NullPointerException exc) { exc.printStackTrace(); return false; } }
		 */

		return true;
	}

	public User getIfValidAuth(String auth) {
		// TODO: Implementare come si vuole la stringa di autenticazione, basta
		// leggere l'User e confrontare la password comunque, es.:
		// String[] splitted = auth.split(":");
		// if (splitted.length != 2)
		// return null;
		//
		// User user = new User(splitted[0], splitted[1]);
		// for (User localUser : this)
		// if (localUser.equals(user))
		// return user;

		// Attualmente leggiamo solo dal properties una password comune, in
		// futuro bisognerà implementare il sistema utenti da database per
		// l'associazione dei file

		return null;
	}
}
