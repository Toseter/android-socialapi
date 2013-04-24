android-socialapi
=================
Это небольшая обёртка для Android'а, предоставляющая единый доступ к социальным сервисам (ВКонтакте, Facebook и Twitter). Она позволяет:

1. Публиковать сообщения на стенку пользователю (в т.ч. с изображением)
1. Брать базовую информацию о пользователе, если соц. сервис её предоставляет (имя, фамилия, email, день рождения, пол и адрес аватарки 100x100).

### Hello World
```
public class MainActivity extends Activity implements OnUserLoggedInListener, OnWallPostedListener {

	private static final int onActivityResultRequestCode = 1;
	User user;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = SocialNetwork.getUser(this, SocialNetwork.VK, onActivityResultRequestCode);
		if (!user.isLoggedIn()){
			user.login(this);
		} else {
			wallPost();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		user.onActivityResult(requestCode, resultCode, data);
	}
	
	private void wallPost() {
		user.wallPost("Hello world", this);
	}

	public void onLoggedIn(boolean isFail, Throwable exception) {
		if (isFail){
			System.exit(-1);
		}
		Toast.makeText(this, user.getFirstName() + " has been successfully logged in", Toast.LENGTH_LONG).show();
		wallPost();
	}

	public void onWallPosted(boolean isFail, Throwable exception) {
		Toast.makeText(this, "Message was wall posted", Toast.LENGTH_LONG).show();
	}
}
```