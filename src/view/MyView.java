package view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.receivers.display.Displayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.Observable;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyView extends Observable implements View, Initializable {

	@FXML private GUIDisplayer GUIDisplayer;
	private MediaPlayer mediaPlayer;
	@FXML private Label counter;
	@FXML private Label timer;
	private Timeline timeline;
	private int seconds, minutes, hours;
	boolean levelLoaded = false;


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

        showSteps();
	    loadAndPlayMusic();
		GUIDisplayer.showLogo();
		GUIDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e)->{ GUIDisplayer.requestFocus(); });

		GUIDisplayer.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {

				if(event.getCode() == KeyCode.UP) {
					setChanged();
					notifyObservers("move up");
				}
				if(event.getCode() == KeyCode.DOWN) {
					setChanged();
					notifyObservers("move down");
				}
				if(event.getCode() == KeyCode.LEFT) {
					setChanged();
					notifyObservers("move left");
				}
				if(event.getCode() == KeyCode.RIGHT) {
					setChanged();
					notifyObservers("move right");
				}
			}
		});
	}

    @Override
    public Displayer getDisplayer() {return this.GUIDisplayer;}

	@Override
	public void displayLevel() {

		try{ GUIDisplayer.display(); }
		catch(Exception e) {passException(e);}
        showSteps();

    }
	
	public void loadLevel() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Open File");
		fc.setInitialDirectory(new File (System.getProperty("user.dir")));
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Text","*.txt"),
				new FileChooser.ExtensionFilter("Object","*.obj"),
				new FileChooser.ExtensionFilter("XML","*.xml")
		);

		File chosen = fc.showOpenDialog(null);
		if(chosen == null)
			return;
		String loadCommand = "load "+chosen.getAbsolutePath();
		setChanged();
		notifyObservers(loadCommand);
		System.out.println("load command sent: " + loadCommand);
		stopTimer();
		startTimer();
		levelLoaded = true;
	}

	public void saveLevel() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save File");
		fc.setInitialDirectory(new File (System.getProperty("user.dir")));
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Object","*.obj"),
				new FileChooser.ExtensionFilter("XML","*.xml")
		);

		File chosen = fc.showSaveDialog(null);
		if(chosen == null)
			return;
		String saveCommand = "save "+chosen.getAbsolutePath();
		setChanged();
		notifyObservers(saveCommand);
		System.out.println("save command sent: " + saveCommand);
	}

	public void exit() {
		setChanged();
		notifyObservers("exit");
		Platform.exit();
		System.exit(0);
	}

	public void passException(Exception e) {
		Platform.runLater(() -> {
			if(e.getMessage().equals("save completed!")) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Save File");
				alert.setHeaderText("The file was saved successfully");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
			else if(e.getMessage().equals("good job..")) {
				pauseTimer();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Level Completed");
				alert.setHeaderText("Level is completed!");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
				saveScore();
			}
			else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Error occurred!");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		});
	}

	public void saveScore() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("High Score Table");
		alert.setHeaderText("Save your score to database!");
		alert.setContentText("Would you like to save your score for this level?");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK){
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("High Score Table");
			dialog.setContentText("Please enter your name");
			Optional<String> userName = dialog.showAndWait();
			if(userName.isPresent()){
				setChanged();
				notifyObservers("saveScore "+userName.get());
			}
		}
		else {
			alert.close();
		}


	}

	public void about() {
		Platform.runLater( () -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setHeaderText("Sokoban");
			alert.setContentText("created by Omer Gafni");
			alert.showAndWait();
		});
	}

	public void restartLevel() {
		setChanged();
		notifyObservers("restart");
		stopTimer();
		startTimer();
	}

	public void pauseMusic() {mediaPlayer.pause();}

	public void playMusic() {mediaPlayer.play();}

	private void loadAndPlayMusic() {
		URL fileUrl = MyView.class.getResource("/resources/music/sokoMusic.mp3");
		javafx.scene.media.Media musicFile = new javafx.scene.media.Media(fileUrl.toExternalForm());
		this.mediaPlayer = new MediaPlayer(musicFile);
		this.mediaPlayer.setAutoPlay(true);
	}

    public void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(0), e ->timeDuration()), new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void pauseTimer() {timeline.pause();}

    public void stopTimer() {
		if(timeline != null) timeline.stop();
		this.minutes = 0;
		this.seconds = 0;
		this.hours = 0;
	}

	public Time getTimeStamp(){return new Time(this.hours,this.minutes,this.seconds);}

    private void timeDuration() {
        if (seconds < 59) {
            seconds++;
        } else {
            seconds = 0;
            if (minutes < 59) {
                minutes++;
            }else{
                minutes = 0;
                hours++;
            }
        }
        timer.setText("Time: "+Integer.toString(hours)+"h"+Integer.toString(minutes)+"m"+Integer.toString(seconds)+"s");
    }

    private void showSteps(){
	    int c;
	    if(GUIDisplayer.getLevel() == null) {
            c = 0;
	    }
        else {
            c = GUIDisplayer.getLevel().getStepsCounter();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                counter.setText("Steps counter: "+Integer.toString(c));
            }
        });
    }

    public void showScoreTable(){
    	if(GUIDisplayer.getLevel() == null) {
			passException(new Exception("Load level first.."));
			return;
    	}
    	Platform.runLater( () -> {
			try {
    			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HighScoresWindow.fxml"));
				Parent root = fxmlLoader.load();
				HighScoresWindowController c = fxmlLoader.getController();
				c.buildData(this.GUIDisplayer.getLevel().getLevelName());
				Stage stage = new Stage();
				stage.setScene(new Scene(root));
				stage.setTitle("High Scores");
				stage.showAndWait();
			} catch (IOException e) {e.printStackTrace();}
		});
	}

	public void solve(){
		if(!levelLoaded){
			passException(new IOException("Please load a level first"));
			return;
		}
		setChanged();
		notifyObservers("solve");
	}

	public void solutionHandler(String[] solution){

		if(solution.length == 0) {
			passException(new Exception("Solution service is not available"));
			return;
		}

		restartLevel();
		Platform.runLater(()->{
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Level Solver");
			alert.setHeaderText("Your solution has arrived!");
			alert.setContentText("Choose your option:");

			ButtonType buttonTypeSolve = new ButtonType("Solve");
			ButtonType buttonTypeHint = new ButtonType("Hint");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(buttonTypeSolve, buttonTypeHint, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonTypeSolve){
				new Thread(()->{
					for(String s : solution){
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						setChanged();
						notifyObservers(s);
					}
				}).start();

			} else if (result.get() == buttonTypeHint) {
				Platform.runLater(()->{
					Alert a = new Alert(Alert.AlertType.INFORMATION);
					a.setTitle("Hint");
					a.setHeaderText(null);
					// Building the hint
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i<solution.length/2; i++){
						sb.append(solution[i]+"->");
					}
					sb.delete(sb.lastIndexOf("->"),sb.lastIndexOf("->")+2);
					a.setContentText(sb.toString());
					a.showAndWait();

				});
			} else {
				alert.close();
			}
		});

	}

}
