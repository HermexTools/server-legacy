import java.io.File;

/**
 * 
 * @author Flegyas
 *
 */
public class User implements Comparable<User> {
	private String nickname, password;
	private int lastIndex;

	public User(String nickname, String password) {
		this.nickname = nickname;
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}

	private int incrementLastIndex() {
		lastIndex++;
		// Users.getLocalInstance().save();
		return lastIndex;
	}

	public File newPuushFile() {
		return new File(new File(Constants.OWP_DIR, nickname),
				incrementLastIndex() + ".jpeg");
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		return other.getNickname().equalsIgnoreCase(getNickname())
				&& other.getPassword().equals(getPassword());
	}

	@Override
	public int compareTo(User other) {
		return other.getNickname().compareTo(getNickname());
	}
}
