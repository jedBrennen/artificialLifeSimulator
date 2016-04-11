package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import artificialLifeSimulator.AWorld;
import artificialLifeSimulator.AWorldObject;
import artificialLifeSimulator.Configuration;
import artificialLifeSimulator.Direction;
import worldObjects.AFood;
import worldObjects.ALifeForm;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import lifeFormTypes.ACarnivore;
import lifeFormTypes.AHerbivore;

/**
 * @author Jed Brennen
 *
 */
public class MainScreen extends Application {
	private final MenuBar menuBar = new MenuBar();
	private Menu fileMenu, editMenu, viewMenu, simMenu, helpMenu;
	private MenuItem fileNew, fileLoad, fileSave, fileSaveAs, fileExit, editConfig, editModify, editAdd, editRemove,
			viewConfig, viewLife, viewWorldInfo, viewWorld, simPlay, simPause, simRewind, simReset, helpApp, helpAuthor;
	private Button playPauseButton, rewindButton, resetButton, addLifeButton, removeLifeButton;
	private Slider speedSlider;
	private ComboBox<String> addLifeTypeCombo;

	private Scene scene;
	private BorderPane rootPane;
	private VBox menuPane;
	private BorderPane sidePane;
	private ScrollPane infoScrollPane;
	private StackPane infoPane;
	private GridPane simPane, configInfoPane, lifeInfoPane, worldInfoPane;
	private VBox simControlsPane;
	private HBox playbackButtons, playbackSlider, lifeFormButtons;

	private Timeline timeline;

	private int width, height;

	private VBox[][] worldContainers;

	private boolean saved = true, playing = false;
	private ButtonType buttonClicked;

	private Configuration config;
	private AWorld world;

	@Override
	public void start(Stage primaryStage) {

		setupScene();

		scene = new Scene(rootPane);
		primaryStage.setScene(scene);

		setupKeyCombos();

		setupHandlers();

		setupConfiguration();

		primaryStage.getIcons().add(new Image("file:resources/images/heart.png"));
		primaryStage.setTitle("Artificial Life Simulator v.1");
		primaryStage.show();

		viewWorldInfo.fire();

	}

	/**
	 * Loads the configuration file and world file using the lastConfig.txt file
	 * to load the last used configuration. If lastConfig.txt or the
	 * last/default file is missing the method will generate a new
	 * configuration.
	 */
	private void setupConfiguration() {
		File f = new File("lastConfig.txt");
		String filePath = "";
		if (f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				filePath = br.readLine();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!f.exists() || !(new File(filePath).exists())) {
			config = new Configuration();
			Optional<Settings> settingsResult = null;
			settingsResult = showSetupDialog();
			if (!settingsResult.isPresent()) {
				new File("lastConfig.txt").delete();
				new File("default.xml").delete();
				System.exit(0);
			}
			world = new AWorld(config);
			showLifeFormDialog(null);
			world.placeFood(true);
			world.placeObstacles(true);
			world.save();
			saved = true;
		} else {
			config = new Configuration();
			world = new AWorld();
			world.load(config);
			world.placeFood(false);
			world.placeObstacles(false);
			config.save();
			world.save();
			saved = true;
		}
	}

	/**
	 * Initialises all objects that makeup the scene and loads them in to the
	 * various panes and root pane.
	 */
	private void setupScene() {
		width = 920;
		height = 530;

		fileMenu = new Menu("File");
		fileNew = new MenuItem("New Configuration...");
		fileLoad = new MenuItem("Load configuration...");
		fileSave = new MenuItem("Save");
		fileSaveAs = new MenuItem("Save as...");
		fileExit = new MenuItem("Exit");
		fileMenu.getItems().addAll(fileNew, fileLoad, fileSave, fileSaveAs, fileExit);

		editMenu = new Menu("Edit");
		editConfig = new MenuItem("Edit Configuration");
		editAdd = new MenuItem("Add Life Form");
		editModify = new MenuItem("Modify Life Forms");
		editRemove = new MenuItem("Remove Life Form");
		editMenu.getItems().addAll(editConfig, editAdd, editModify, editRemove);

		viewMenu = new Menu("View");
		viewConfig = new MenuItem("Display Configuration");
		viewLife = new MenuItem("Display Life Form Information");
		viewWorldInfo = new MenuItem("Display World Information");
		viewWorld = new MenuItem("Display Loaded World");
		viewMenu.getItems().addAll(viewConfig, viewLife, viewWorldInfo, viewWorld);

		simMenu = new Menu("Simulation");
		simPlay = new MenuItem("Play");
		simPause = new MenuItem("Pause");
		simRewind = new MenuItem("Rewind");
		simReset = new MenuItem("Reset");
		simMenu.getItems().addAll(simPlay, simPause, simRewind, simReset);

		helpMenu = new Menu("Help");
		helpApp = new MenuItem("Application Information");
		helpAuthor = new MenuItem("Author Information");
		helpMenu.getItems().addAll(helpApp, helpAuthor);

		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, simMenu, helpMenu);

		menuPane = new VBox();
		menuPane.setPrefWidth(width);
		menuPane.getChildren().add(menuBar);

		simPane = new GridPane();
		simPane.setId("simPane");
		simPane.getStylesheets().add("file:resources/css/simPane.css");
		simPane.setPrefWidth((2 * width) / 3);
		simPane.setGridLinesVisible(false);

		configInfoPane = new GridPane();
		configInfoPane.setGridLinesVisible(false);
		configInfoPane.setVgap(10);

		lifeInfoPane = new GridPane();
		lifeInfoPane.setGridLinesVisible(false);
		lifeInfoPane.setVgap(10);

		worldInfoPane = new GridPane();
		worldInfoPane.setGridLinesVisible(false);
		worldInfoPane.setVgap(10);

		infoPane = new StackPane();
		infoPane.setPrefWidth(width / 3);
		infoPane.setPrefHeight((2 * height) / 3);
		infoPane.getStylesheets().add("file:resources/css/infoPane.css");

		infoScrollPane = new ScrollPane();
		infoScrollPane.setFitToWidth(true);
		infoScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		infoScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		infoScrollPane.setPadding(new Insets(20, 0, 0, 20));
		infoScrollPane.setContent(infoPane);

		playPauseButton = new Button("Play");
		playPauseButton.setMaxWidth(Double.MAX_VALUE);
		rewindButton = new Button("Rewind");
		rewindButton.setMaxWidth(Double.MAX_VALUE);
		resetButton = new Button("Reset");
		resetButton.setMaxWidth(Double.MAX_VALUE);

		speedSlider = new Slider(0, 10, 1);
		speedSlider.setShowTickMarks(true);
		speedSlider.setShowTickLabels(true);
		speedSlider.setSnapToTicks(true);
		speedSlider.setMajorTickUnit(5);
		speedSlider.setMinorTickCount(4);
		speedSlider.setBlockIncrement(1);

		addLifeButton = new Button("+");
		removeLifeButton = new Button("-");

		addLifeTypeCombo = new ComboBox<String>();
		addLifeTypeCombo.getItems().addAll("Herbivore", "Carnivore");
		addLifeTypeCombo.setValue("Herbivore");

		playbackButtons = new HBox();
		playbackButtons.setPadding(new Insets(20, 0, 10, 0));
		playbackButtons.setSpacing(20);
		playbackButtons.setAlignment(Pos.CENTER);
		playbackButtons.getChildren().addAll(rewindButton, playPauseButton, resetButton);

		playbackSlider = new HBox();
		playbackSlider.setPadding(new Insets(10, 0, 10, 0));
		playbackSlider.setAlignment(Pos.CENTER);
		playbackSlider.getChildren().add(speedSlider);

		lifeFormButtons = new HBox();
		lifeFormButtons.setPadding(new Insets(10, 0, 20, 0));
		lifeFormButtons.setSpacing(20);
		lifeFormButtons.setAlignment(Pos.CENTER);
		lifeFormButtons.getChildren().addAll(addLifeTypeCombo, addLifeButton, removeLifeButton);

		simControlsPane = new VBox();
		simControlsPane.setPrefHeight(height / 3);
		simControlsPane.getChildren().addAll(new Separator(), playbackButtons, playbackSlider, lifeFormButtons);

		sidePane = new BorderPane();
		sidePane.setCenter(infoScrollPane);
		sidePane.setBottom(simControlsPane);

		rootPane = new BorderPane();
		rootPane.setTop(menuPane);
		rootPane.setCenter(simPane);
		rootPane.setRight(sidePane);
		rootPane.setPrefHeight(height);
		rootPane.setPrefWidth(width);

	}

	/**
	 * Sets up the shortcuts for the program.
	 */
	private void setupKeyCombos() {
		fileNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		fileSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

		editAdd.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));

		viewConfig.setAccelerator(
				new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		viewLife.setAccelerator(
				new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		viewWorldInfo.setAccelerator(
				new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		viewWorld.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

		simPlay.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		simReset.setAccelerator(
				new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		simRewind.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));

		helpApp.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
	}

	/**
	 * Sets all the handlers for the buttons in the scene and the scene itself.
	 */
	private void setupHandlers() {

		scene.getWindow().setOnCloseRequest((event) -> {
			if (playing == true) {
				timeline.pause();
				playing = false;
			}

			if (saved == false) {
				Optional<ButtonType> result = showSaveDialog();

				if (result.get().getButtonData() == ButtonData.YES) {
					if (playing) {
						timeline.stop();
						playing = false;
					}
					config.save();
					world.save();
				} else if (result.get().getButtonData() == ButtonData.NO) {
					if (playing) {
						timeline.stop();
						playing = false;
					}
					return;
				} else {
					if (timeline != null) {
						timeline.play();
						playing = true;
					}
					event.consume();
				}
			}
		});

		scene.setOnMouseClicked((event) -> {

		});

		fileNew.setOnAction((event) -> {
			if (playing) {
				timeline.pause();
				playing = false;
			}

			Optional<ButtonType> result = null;

			if (saved == false) {
				result = showSaveDialog();

				if (result.get().getButtonData() == ButtonData.YES) {
					config.save();
					world.save();
				} else if (result.get().getButtonData() != ButtonData.NO) {
					timeline.play();
					playing = true;
					event.consume();
				}
			}

			if (result == null || (result.get().getButtonData() == ButtonData.YES
					|| result.get().getButtonData() == ButtonData.NO)) {
				if (playing) {
					timeline.stop();
					playing = false;
				}
				Optional<Settings> configResult = showConfigurationDialog(false);
				if (configResult.isPresent()) {
					clearSimPane();
					clearInfoPane();
					viewWorldInfo.fire();
				}
			} else {
				if (!playing) {
					timeline.play();
					playing = true;
				}
			}

		});

		fileLoad.setOnAction((event) -> {
			if (playing) {
				timeline.pause();
				playing = false;
			}

			Optional<ButtonType> result = null;
			if (saved == false) {
				result = showSaveDialog();

				if (result.get().getButtonData() == ButtonData.YES) {
					config.save();
					world.save();
				} else if (result.get().getButtonData() != ButtonData.NO) {
					timeline.play();
					playing = true;
					event.consume();
				}
			}

			if (result == null || (result.get().getButtonData() == ButtonData.YES
					|| result.get().getButtonData() == ButtonData.NO)) {
				final FileChooser fc = new FileChooser();
				fc.setTitle("Open configuration");
				fc.setInitialDirectory(config.getFile().getAbsoluteFile().getParentFile());
				fc.getExtensionFilters().add(new ExtensionFilter("Configuration files (*.xml)", "*.xml"));

				File chosen = null;
				boolean validFile = false;

				do {

					chosen = fc.showOpenDialog(this.scene.getWindow());

					if (chosen != null) {
						if (chosen.isFile()) {
							String extension = config.getExtension(chosen);

							if (extension.equalsIgnoreCase("xml")) {
								timeline.stop();
								playing = false;
								config.load(chosen.getName());
								world = new AWorld();
								world.load(config);
								validFile = true;
							} else {
								showAlert(AlertType.ERROR, "Invalid File", null,
										"You have selected an invalid file, please select an xml file.");
							}
						}
						clearSimPane();
						clearInfoPane();
					} else {
						timeline.play();
						playing = true;
						validFile = true;
						break;
					}
				} while (!validFile);
			}

		});

		fileSave.setOnAction((event) -> {

			config.save();
			world.save();
			saved = true;

		});

		fileSaveAs.setOnAction((event) -> {

			final FileChooser fc = new FileChooser();
			fc.setTitle("Save configuration");
			fc.setInitialDirectory(config.getFile().getAbsoluteFile().getParentFile());
			fc.getExtensionFilters().add(new ExtensionFilter("Configuration files (*.xml)", "*.xml"));

			File savedFile = fc.showSaveDialog(this.scene.getWindow());

			if (savedFile != null) {
				if (config.getExtension(savedFile).equalsIgnoreCase("xml")) {
					config.saveAs(savedFile);
				} else {
					config.saveAs(new File(savedFile.getAbsolutePath() + ".xml"));
				}
				world.save();
				saved = true;
			}

		});

		fileExit.setOnAction((event) -> {

			scene.getWindow().fireEvent(new WindowEvent(scene.getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));

		});

		editConfig.setOnAction((event) -> {

			Optional<Settings> result = showConfigurationDialog(true);
			if (result.isPresent()) {
				updateInfoPane();
			}

		});

		editAdd.setOnAction((event) -> {

			boolean mapFull = true;
			for (AWorldObject[] mapY : world.getMap()) {
				for (AWorldObject mapX : mapY) {
					if (!(mapX instanceof ALifeForm)) {
						mapFull = false;
					}
				}
			}

			if (mapFull) {

				showAlert(AlertType.WARNING, "World Full", "The world is to full to add a bug",
						"To add a new bug either remove an existing one or increase the world dimensions");
			} else {

				Optional<ALifeForm> result = showLifeFormDialog(null);
				if (result.isPresent()) {
					world.clearMap();
					for (ALifeForm a : world.getLifeFormList()) {
						world.placeLifeForm(a);
					}
					world.placeFood(false);
					world.placeObstacles(false);

					if (worldContainers != null) {
						showWorld();
					}
					updateInfoPane();
				}
			}

		});

		editModify.setOnAction((event) -> {

			List<ALifeForm> lifeForms = world.getLifeFormList();

			if (!lifeForms.isEmpty()) {
				ChoiceDialog<ALifeForm> dialog = new ChoiceDialog<>();

				DialogPane dPane = dialog.getDialogPane();

				Stage dStage = (Stage) dPane.getScene().getWindow();
				dStage.getIcons().add(new Image("file:resources/images/invader.png"));

				dialog.setTitle("Choose a LifeForm");
				dialog.setHeaderText("Select a LifeForm to modify");
				dialog.setContentText("Choose a LifeForm:");

				for (ALifeForm a : lifeForms) {
					dialog.getItems().add(a);
				}

				Optional<ALifeForm> result = dialog.showAndWait();

				if (result.isPresent()) {
					result = showLifeFormDialog(result.get());
					if (result.isPresent()) {
						world.clearMap();
						for (ALifeForm a : world.getLifeFormList()) {
							world.placeLifeForm(a);
						}
						world.placeFood(false);
						world.placeObstacles(false);

						if (worldContainers != null) {
							showWorld();
						}
						updateInfoPane();
					}
				}

			} else {
				Optional<ButtonType> result = showAlert(AlertType.CONFIRMATION, "No LifeForms",
						"There are currently no LifeForms to edit", "Would you like to create a new life form?");
				if (result.get() == ButtonType.OK) {
					editAdd.fire();
				}
			}

		});

		editRemove.setOnAction((event) -> {

			List<ALifeForm> lifeForms = world.getLifeFormList();

			if (!lifeForms.isEmpty()) {
				ChoiceDialog<ALifeForm> dialog = new ChoiceDialog<>();

				DialogPane dPane = dialog.getDialogPane();

				Stage dStage = (Stage) dPane.getScene().getWindow();
				dStage.getIcons().add(new Image("file:resources/images/invader.png"));

				dialog.setTitle("Choose a LifeForm");
				dialog.setHeaderText("Select a LifeForm to remove");
				dialog.setContentText("Choose a LifeForm:");

				for (ALifeForm a : lifeForms) {
					dialog.getItems().add(a);
				}

				Optional<ALifeForm> result = dialog.showAndWait();

				if (result.isPresent()) {
					world.getLifeFormList().remove(result.get());

					for (ALifeForm a : world.getLifeFormList()) {
						if (a.getID() > result.get().getID()) {
							a.setId(a.getID() - 1);
						}
					}

					world.save();

					world.clearMap();
					for (ALifeForm a : world.getLifeFormList()) {
						world.placeLifeForm(a);
					}
					world.placeFood(false);
					world.placeObstacles(false);

					if (worldContainers != null) {
						showWorld();
					}

					world.save();

					updateInfoPane();
				}

			} else {
				Optional<ButtonType> result = showAlert(AlertType.CONFIRMATION, "No LifeForms",
						"There are currently no LifeForms to remove", "Would you like to create a new life form?");
				if (result.get() == ButtonType.OK) {
					editAdd.fire();
				}
			}

		});

		viewConfig.setOnAction((event) -> {

			Label title = new Label("Simulation Configuration");
			title.setId("header");
			Label nameLabel = new Label("Configuration Name: ");
			nameLabel.setId("propertyID");
			Label name = new Label(config.getFileName(false));
			Label widthLabel = new Label("Map Width: ");
			widthLabel.setId("propertyID");
			Label width = new Label(config.getProperty("mapWidth"));
			Label heightLabel = new Label("Map Height: ");
			heightLabel.setId("propertyID");
			Label height = new Label(config.getProperty("mapHeight"));
			Label foodLabel = new Label("Food Density: ");
			foodLabel.setId("propertyID");
			Label food = new Label(config.getProperty("foodDensity"));
			Label obstacleLabel = new Label("Obstacle Density: ");
			obstacleLabel.setId("propertyID");
			Label obstacle = new Label(config.getProperty("objDensity"));
			Label cyclesLabel = new Label("Simulation Cycles: ");
			cyclesLabel.setId("propertyID");
			Label cycles = new Label(config.getProperty("cycles"));
			Label displayLabel = new Label("Display Iterations: ");
			displayLabel.setId("propertyID");
			Label display = new Label(config.getProperty("displayIterations"));

			configInfoPane.getChildren().clear();

			configInfoPane.add(title, 0, 0);
			configInfoPane.add(nameLabel, 0, 2);
			configInfoPane.add(name, 1, 2);
			configInfoPane.add(widthLabel, 0, 3);
			configInfoPane.add(width, 1, 3);
			configInfoPane.add(heightLabel, 0, 4);
			configInfoPane.add(height, 1, 4);
			configInfoPane.add(foodLabel, 0, 5);
			configInfoPane.add(food, 1, 5);
			configInfoPane.add(obstacleLabel, 0, 6);
			configInfoPane.add(obstacle, 1, 6);
			configInfoPane.add(cyclesLabel, 0, 7);
			configInfoPane.add(cycles, 1, 7);
			configInfoPane.add(displayLabel, 0, 8);
			configInfoPane.add(display, 1, 8);

			infoPane.getChildren().clear();
			infoPane.getChildren().add(configInfoPane);

		});

		viewLife.setOnAction((event) -> {

			lifeInfoPane.getChildren().clear();

			Label title = new Label("LifeForm Information");
			title.setId("header");

			lifeInfoPane.add(title, 0, 0);

			int row = 2;

			if (world.getLifeFormList().isEmpty()) {
				lifeInfoPane.add(new Label("There are no LifeForms to display."), 0, 2);
			} else {

				for (ALifeForm a : world.getLifeFormList()) {
					Label id = new Label("#" + String.valueOf(a.getID()));
					if (!a.isAlive()) {
						id.setText(id.getText() + " (Dead)");
					} else if (a.getPoison() > 0) {
						id.setText(id.getText() + " (Poisoned)");
					}
					id.setId("propertyID");

					lifeInfoPane.add(id, 0, row);
					lifeInfoPane.add(new Label("Name: "), 0, row + 1);
					lifeInfoPane.add(new Label(a.getName()), 1, row + 1);
					lifeInfoPane.add(new Label("Species: "), 0, row + 2);
					lifeInfoPane.add(new Label(a.getSpecies()), 1, row + 2);
					lifeInfoPane.add(new Label("Coordinates: "), 0, row + 3);
					lifeInfoPane.add(new Label((a.getX() + 1) + ", " + (a.getY() + 1)), 1, row + 3);
					lifeInfoPane.add(new Label("Energy: "), 0, row + 4);
					lifeInfoPane.add(new Label(String.valueOf(a.getEnergy())), 1, row + 4);
					lifeInfoPane.add(new Label("Sensing Distance: "), 0, row + 5);
					lifeInfoPane.add(new Label(String.valueOf(a.getSensingDistance())), 1, row + 5);
					row += 9;
				}
			}

			infoPane.getChildren().clear();
			infoPane.getChildren().add(lifeInfoPane);

		});

		viewWorldInfo.setOnAction((event) -> {

			Label title = new Label("World Information");
			title.setId("header");
			Label nameLabel = new Label("World Name: ");
			nameLabel.setId("propertyID");
			Label name = new Label(config.getFileName(false));
			Label widthLabel = new Label("Map Width: ");
			widthLabel.setId("propertyID");
			Label width = new Label(config.getProperty("mapWidth"));
			Label heightLabel = new Label("Map Height: ");
			heightLabel.setId("propertyID");
			Label height = new Label(config.getProperty("mapHeight"));
			Label lifeLabel = new Label("LifeForm Count: ");
			lifeLabel.setId("propertyID");
			Label life = new Label(String.valueOf(world.getLifeFormList().size()));
			Label foodLabel = new Label("Food Count: ");
			foodLabel.setId("propertyID");
			Label food = new Label(String.valueOf(world.getFoodList().size()));
			Label obstacleLabel = new Label("Obstacle Count: ");
			obstacleLabel.setId("propertyID");
			Label obstacle = new Label(String.valueOf(world.getObstacleList().size()));

			worldInfoPane.getChildren().clear();

			worldInfoPane.add(title, 0, 0);
			worldInfoPane.add(nameLabel, 0, 2);
			worldInfoPane.add(name, 1, 2);
			worldInfoPane.add(widthLabel, 0, 3);
			worldInfoPane.add(width, 1, 3);
			worldInfoPane.add(heightLabel, 0, 4);
			worldInfoPane.add(height, 1, 4);
			worldInfoPane.add(lifeLabel, 0, 5);
			worldInfoPane.add(life, 1, 5);
			worldInfoPane.add(foodLabel, 0, 6);
			worldInfoPane.add(food, 1, 6);
			worldInfoPane.add(obstacleLabel, 0, 7);
			worldInfoPane.add(obstacle, 1, 7);

			infoPane.getChildren().clear();
			infoPane.getChildren().add(worldInfoPane);

		});

		viewWorld.setOnAction((event) -> {

			showWorld();

		});

		simPlay.setOnAction((event) -> {

			if (timeline == null) {
				runSimulation();
			} else {
				if (timeline.getStatus() == Animation.Status.PAUSED
						|| timeline.getStatus() == Animation.Status.STOPPED) {
					timeline.play();
					playing = true;
				} else if (timeline.getStatus() == Animation.Status.RUNNING) {
					timeline.pause();
				}
			}

		});

		simPause.setOnAction((event) -> {

			if (timeline != null) {
				if (timeline.getStatus() == Animation.Status.PAUSED) {
					timeline.play();
					playing = true;
				} else if (timeline.getStatus() == Animation.Status.RUNNING) {
					timeline.pause();
					playing = false;
				}
			}

		});

		simRewind.setOnAction((event) -> {
			if (playing) {
				timeline.stop();
				playing = false;
			}
			timeline = null;

			world.clearMap();
			clearSimPane();

			for (ALifeForm a : world.getLifeFormList()) {
				ALifeForm temp = (ALifeForm) a.getInitialLifeForm();
				a.setX(temp.getX());
				a.setY(temp.getY());
				a.setEnergy(temp.getEnergy());
				a.setPoison(temp.getPoison());
				a.setAlive(temp.isAlive());
				a.setTimer(0);
				world.placeLifeForm(a);
			}

			for (AWorldObject a : world.getFoodList()) {
				a.setTimer(((AFood) a).getInitialFood().getTimer());
			}

			world.placeFood(false);
			world.placeObstacles(false);

			updateInfoPane();
			if (worldContainers != null) {
				showWorld();
			}
		});

		simReset.setOnAction((event) -> {
			if (playing) {
				timeline.stop();
				playing = false;
			}
			timeline = null;

			world.clearMap();

			for (ALifeForm a : world.getLifeFormList()) {
				a.setY((int) Math.round(Math.random() * (world.getHeight() - 1)));
				a.setX((int) Math.round(Math.random() * (world.getWidth() - 1)));
				a.setEnergy(a.getInitialLifeForm().getEnergy());
				a.setPoison(a.getInitialLifeForm().getPoison());
				a.isAlive();
				a.setTimer(0);
				a.resetInitialLifeForm();
				world.placeLifeForm(a);
			}

			world.placeFood(true);
			world.placeObstacles(true);

			updateInfoPane();
			if (worldContainers != null) {
				showWorld();
			}

			world.save();
		});

		helpApp.setOnAction((event) -> {

			String content = "Artificial Life Simulator Version 1 \n";
			content += "Created by Jed Brennen\n";
			content += "Coded using JDK 1.8";
			showAlert(AlertType.INFORMATION, "Application Information", "Artificial Life Simulator V.1", content);

		});

		helpAuthor.setOnAction((event) -> {

			String content = "Author: Jed Brennen \n";
			content += "Student Number: 23011508\n";
			content += "Part 2 Computer Science Student\n";
			content += "School of Systems Engineering\n";
			content += "University of Reading";
			showAlert(AlertType.INFORMATION, "Author Information", "Artificial Life Simulator V.1", content);

		});

		playPauseButton.setOnAction((event) -> {

			if (timeline == null) {
				runSimulation();
			} else if (timeline.getStatus() == Animation.Status.RUNNING) {
				timeline.pause();
				playing = false;
			} else if (timeline.getStatus() == Animation.Status.PAUSED
					|| timeline.getStatus() == Animation.Status.STOPPED) {
				timeline.play();
				playing = true;
			}

		});

		rewindButton.setOnAction((event) ->

		{

			if (playing) {
				timeline.stop();
				playing = false;
			}
			timeline = null;

			world.clearMap();
			clearSimPane();

			for (ALifeForm a : world.getLifeFormList()) {
				ALifeForm temp = (ALifeForm) a.getInitialLifeForm();
				a.setX(temp.getX());
				a.setY(temp.getY());
				a.setEnergy(temp.getEnergy());
				a.setPoison(temp.getPoison());
				a.setAlive(temp.isAlive());
				a.setTimer(0);
				world.placeLifeForm(a);
			}

			for (AWorldObject a : world.getFoodList()) {
				a.setTimer(((AFood) a).getInitialFood().getTimer());
			}

			world.placeFood(false);
			world.placeObstacles(false);

			updateInfoPane();
			if (worldContainers != null) {
				showWorld();
			}

		});

		resetButton.setOnAction((event) ->

		{

			if (playing) {
				timeline.stop();
				playing = false;
			}
			timeline = null;

			world.clearMap();

			for (ALifeForm a : world.getLifeFormList()) {
				a.setY((int) Math.round(Math.random() * (world.getHeight() - 1)));
				a.setX((int) Math.round(Math.random() * (world.getWidth() - 1)));
				a.setEnergy(a.getInitialLifeForm().getEnergy());
				a.setPoison(a.getInitialLifeForm().getPoison());
				a.isAlive();
				a.setTimer(0);
				a.resetInitialLifeForm();
				world.placeLifeForm(a);
			}

			world.placeFood(true);
			world.placeObstacles(true);

			updateInfoPane();
			if (worldContainers != null) {
				showWorld();
			}

			world.save();

		});

		addLifeButton.setOnAction((event) ->

		{
			boolean mapFull = true;
			for (AWorldObject[] mapY : world.getMap()) {
				for (AWorldObject mapX : mapY) {
					if (!(mapX instanceof ALifeForm)) {
						mapFull = false;
					}
				}
			}

			if (mapFull) {

				showAlert(AlertType.WARNING, "World Full", "The world is to full to add a bug",
						"To add a new bug either remove an existing one or increase the world dimensions");

			} else {

				ALifeForm lf;
				int id = world.getLifeFormList().size() + 1;

				if (addLifeTypeCombo.getValue().equalsIgnoreCase("Herbivore")) {
					lf = new AHerbivore(world, "LF " + id, 10, id);
				} else {
					lf = new ACarnivore(world, "LF " + id, 10, id);
				}

				world.getLifeFormList().add(lf);

				world.placeLifeForm(lf);

				world.save();

				if (worldContainers != null) {
					showWorld();
				}

				updateInfoPane();

			}

		});

		removeLifeButton.setOnAction((event) ->

		{
			ALifeForm lf = world.getLifeFormList().get(world.getLifeFormList().size() - 1);

			world.setMapValue(null, lf.getY(), lf.getX());

			world.getLifeFormList().remove(lf);

			if (worldContainers != null) {
				showWorld();
			}

			world.save();

			updateInfoPane();

		});

	}

	/**
	 * Runs the simulation by initialising a timeline with a KeyFrame that
	 * contains all the logic to run each cycle of the program.
	 */
	private void runSimulation() {
		timeline = new Timeline();
		timeline.setCycleCount(Integer.parseInt(config.getProperty("cycles")));
		timeline.setAutoReverse(false);
		timeline.rateProperty().bind(speedSlider.valueProperty());

		showWorld();

		KeyFrame frame = new KeyFrame(Duration.millis(1000), (event) -> {

			AWorldObject senseResult = null;
			Direction d = null;
			int energyGained = 0;

			for (ALifeForm a : world.getLifeFormList()) {

				senseResult = null;
				d = null;
				energyGained = 0;

				// Only continues if the object is not dead.
				if (a.isAlive()) {
					boolean moved = false;
					// If the timer is greater than 0 the LifeForm will not
					// move.
					if (a.getTimer() == 0) {
						do {
							// Gets the direction to move the LifeForm.
							d = a.sense();
							if (d != null) {
								// Moves the bug and gets the object on to which
								// the LifeForm can move.
								senseResult = a.move(d);
								if (senseResult != null) {
									if (a instanceof ACarnivore) {
										if (senseResult instanceof AHerbivore) {
											AHerbivore food = (AHerbivore) senseResult;
											energyGained = (int) Math.ceil(food.getEnergy() / 2);
											a.setEnergy(a.getEnergy() + 2 + energyGained);
											a.setPoison(a.getPoison() + food.getPoison());
											food.setEnergy(0);
											food.setAlive(false);
											moved = true;
										}
									} else if (a instanceof AHerbivore) {
										if (senseResult instanceof AFood) {
											AFood food = (AFood) senseResult;
											if (food.isPoisonous()) {
												energyGained = food.getValue() / 3;
												a.setEnergy(a.getEnergy() + energyGained);
												a.setPoison(a.getPoison() + food.getValue());

												food.setTimer(18 + (int) (food.getValue() * Math.random()));
											} else {
												energyGained = food.getValue();
												a.setEnergy(a.getEnergy() + energyGained);

												food.setTimer(9 + (int) (food.getValue() * Math.random()));
											}
											moved = true;
										}
									}
								} else {
									moved = true;
								}
							} else {
								break;
							}

						} while (!moved);

						a.setEnergy(a.getEnergy() - 1);

						if (energyGained > 0) {
							a.setTimer((int) Math.ceil(energyGained / 2) + 1);
						}

						if (a.getEnergy() <= 0) {
							a.setAlive(false);
						}

					} else {
						a.decrementTimer();
					}

					if (a.getPoison() > 0 && a.isAlive()) {
						a.setEnergy(a.getEnergy() - 1);
						a.setPoison(a.getPoison() - 1);
					}

				}
			}

			for (AWorldObject a : world.getFoodList()) {
				AWorldObject[][] map = world.getMap();
				if (a.getTimer() > 0) {
					if (map[a.getY()][a.getX()] == null) {
						a.decrementTimer();
					} else if (map[a.getY()][a.getX()] instanceof ALifeForm) {
						if (map[a.getY()][a.getX()].getTimer() == 0) {
							a.decrementTimer();
						}
					}
				}
			}

			world.placeFood(false);

			updateInfoPane();

			boolean displayIterations = Boolean.parseBoolean(config.getProperty("displayIterations"));
			if (displayIterations) {
				showWorld();
			}

		});

		timeline.getKeyFrames().add(frame);
		timeline.play();
		playing = true;
		saved = false;
		timeline.setOnFinished((event) ->

		{

			Platform.runLater(() -> {

				playing = false;
				showAlert(AlertType.INFORMATION, "Simulation Finished", "", "The simulation has completed all cycles");
				showWorld();

			});

		});

	}

	private void showWorld() {
		clearSimPane();

		worldContainers = new VBox[world.getHeight()][world.getWidth()];
		AWorldObject[][] map = world.getMap();

		int height = Integer.parseInt(config.getProperty("mapHeight"));
		int width = Integer.parseInt(config.getProperty("mapWidth"));

		for (int y = 0; y < height; y++) {
			RowConstraints row = new RowConstraints();
			row.setMinHeight(10);
			row.setPrefHeight(100);
			row.setMaxHeight(1000);
			row.setVgrow(Priority.ALWAYS);
			simPane.getRowConstraints().add(row);
		}

		for (int x = 0; x < width; x++) {
			ColumnConstraints col = new ColumnConstraints();
			col.setMinWidth(10);
			col.setPrefWidth(100);
			col.setMaxWidth(1000);
			col.setHgrow(Priority.ALWAYS);
			simPane.getColumnConstraints().add(col);
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				worldContainers[y][x] = new VBox();
				worldContainers[y][x].setAlignment(Pos.CENTER);
				if (map[y][x] != null) {
					if (map[y][x] instanceof ALifeForm) {
						if (((ALifeForm) map[y][x]).isAlive()) {
							ImageView img = new ImageView(new Image(map[y][x].getSymbol()));
							worldContainers[y][x].getChildren().add(img);
							worldContainers[y][x].setAlignment(Pos.CENTER);
							img.setFitHeight(50);
							img.setFitWidth(50);
							img.fitHeightProperty().bind(worldContainers[y][x].heightProperty());
							img.fitWidthProperty().bind(worldContainers[y][x].widthProperty());
							worldContainers[y][x].setMinHeight(Double.MIN_VALUE);
							worldContainers[y][x].setMinWidth(Double.MIN_VALUE);
							worldContainers[y][x].setMaxHeight(Double.MAX_VALUE);
							worldContainers[y][x].setMaxWidth(Double.MAX_VALUE);
							simPane.add(worldContainers[y][x], x, y);
						}
					} else {
						ImageView img = new ImageView(new Image(map[y][x].getSymbol()));
						worldContainers[y][x].getChildren().add(img);
						worldContainers[y][x].setAlignment(Pos.CENTER);
						img.setFitHeight(50);
						img.setFitWidth(50);
						img.fitHeightProperty().bind(worldContainers[y][x].heightProperty());
						img.fitWidthProperty().bind(worldContainers[y][x].widthProperty());
						worldContainers[y][x].setMinHeight(Double.MIN_VALUE);
						worldContainers[y][x].setMinWidth(Double.MIN_VALUE);
						worldContainers[y][x].setMaxHeight(Double.MAX_VALUE);
						worldContainers[y][x].setMaxWidth(Double.MAX_VALUE);
						simPane.add(worldContainers[y][x], x, y);
					}

				}

			}
		}

	}

	private void clearSimPane() {
		if (simPane != null) {
			if (!simPane.getChildren().isEmpty()) {
				simPane.getRowConstraints().clear();
				simPane.getColumnConstraints().clear();
				simPane.getChildren().clear();
			}
		}
	}

	private void updateInfoPane() {
		if (infoPane.getChildren().size() > 0) {
			if (infoPane.getChildren().get(infoPane.getChildren().size() - 1).equals(configInfoPane)) {
				clearInfoPane();
				viewConfig.fire();
			} else if (infoPane.getChildren().get(infoPane.getChildren().size() - 1).equals(lifeInfoPane)) {
				clearInfoPane();
				viewLife.fire();
			} else if (infoPane.getChildren().get(infoPane.getChildren().size() - 1).equals(worldInfoPane)) {
				clearInfoPane();
				viewWorldInfo.fire();
			}
		}
	}

	private void clearInfoPane() {
		if (infoPane != null && !infoPane.getChildren().isEmpty()) {
			infoPane.getChildren().clear();
		}
	}

	private Optional<ButtonType> showAlert(AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:resources/images/heart.png"));
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		return alert.showAndWait();
	}

	private Optional<ButtonType> showSaveDialog() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:resources/images/heart.png"));
		alert.setTitle("Save Changes?");
		alert.setHeaderText(null);
		alert.setContentText("Would you like to save your changes before exiting?");

		ButtonType yes = new ButtonType("Yes", ButtonData.YES);
		ButtonType no = new ButtonType("No", ButtonData.NO);
		ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(yes, no, cancel);

		return alert.showAndWait();
	}

	private Optional<Settings> showSetupDialog() {
		Dialog<Settings> settingsDialog = new Dialog<>();

		DialogPane dPane = settingsDialog.getDialogPane();
		dPane.getStylesheets().add("file:resources/css/dialog.css");
		final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

		Stage configStage = (Stage) dPane.getScene().getWindow();
		configStage.getIcons().add(new Image("file:resources/images/configuration.png"));

		settingsDialog.setTitle("Setup Simulator");
		settingsDialog.setHeaderText("Setting up simulation");
		settingsDialog.setGraphic(new ImageView(new Image("file:resources/images/configuration.png")));

		final ButtonType ok = new ButtonType("Create", ButtonData.OK_DONE);
		final ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		settingsDialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 10, 10, 10));

		TextField width = new TextField();
		TextField height = new TextField();
		TextField food = new TextField();
		food.setPromptText("(0 - 5)");
		TextField obstacles = new TextField();
		obstacles.setPromptText("(0 - 5)");
		TextField cycles = new TextField();
		Label widthError = new Label(""), heightError = new Label(""), foodError = new Label(""),
				obstaclesError = new Label(""), cyclesError = new Label("");

		ToggleGroup iterations = new ToggleGroup();
		RadioButton on = new RadioButton("On");
		on.setToggleGroup(iterations);
		on.setSelected(true);
		RadioButton off = new RadioButton("Off");
		off.setToggleGroup(iterations);

		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPrefWidth(300);

		grid.add(new Label("Map Width:"), 0, 1);
		grid.add(width, 1, 1);
		grid.add(widthError, 2, 1);
		grid.add(new Label("Map Height:"), 0, 2);
		grid.add(height, 1, 2);
		grid.add(heightError, 2, 2);
		grid.add(new Label("Food Density:"), 0, 3);
		grid.add(food, 1, 3);
		grid.add(foodError, 2, 3);
		grid.add(new Label("Obstacle Density:"), 0, 4);
		grid.add(obstacles, 1, 4);
		grid.add(obstaclesError, 2, 4);
		grid.add(new Label("Number of Cycles:"), 0, 5);
		grid.add(cycles, 1, 5);
		grid.add(cyclesError, 2, 5);
		grid.add(new Label("Show iterations:"), 0, 6);
		grid.add(on, 1, 6);
		grid.add(off, 2, 6);

		grid.getColumnConstraints().addAll(col1, col2, col3);

		settingsDialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> width.requestFocus());

		width.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				width.pseudoClassStateChanged(errorClass, false);
				widthError.setText("");
			}
		});

		height.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				height.pseudoClassStateChanged(errorClass, false);
				heightError.setText("");
			}
		});

		food.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				food.pseudoClassStateChanged(errorClass, false);
				foodError.setText("");
			}
		});

		obstacles.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				obstacles.pseudoClassStateChanged(errorClass, false);
				obstaclesError.setText("");
			}
		});

		cycles.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				cycles.pseudoClassStateChanged(errorClass, false);
				cyclesError.setText("");
			}
		});

		settingsDialog.setOnCloseRequest((event) -> {

			if (buttonClicked == ok) {

				width.pseudoClassStateChanged(errorClass, false);
				height.pseudoClassStateChanged(errorClass, false);
				food.pseudoClassStateChanged(errorClass, false);
				obstacles.pseudoClassStateChanged(errorClass, false);
				cycles.pseudoClassStateChanged(errorClass, false);
				widthError.setText("");
				heightError.setText("");
				foodError.setText("");
				obstaclesError.setText("");
				cyclesError.setText("");

				if (width.getText().equalsIgnoreCase("")) {
					widthError.setText("Please enter a width value");
					width.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(width.getText())) {
					widthError.setText("Please enter a valid integer");
					width.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (height.getText().equalsIgnoreCase("")) {
					heightError.setText("Please enter a height value");
					height.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(height.getText())) {
					heightError.setText("Please enter a valid integer");
					height.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (food.getText().equalsIgnoreCase("")) {
					foodError.setText("Please enter a food density value");
					food.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(food.getText())) {
					foodError.setText("Please enter a valid integer");
					food.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else {
					int i = Integer.parseInt(food.getText());
					if (i < 0 || i > 5) {
						foodError.setText("Please enter an integer between 0 and 5");
						food.pseudoClassStateChanged(errorClass, true);
						buttonClicked = cancel;
						event.consume();
					}
				}

				if (obstacles.getText().equalsIgnoreCase("")) {
					obstaclesError.setText("Please enter an obstacle density value");
					obstacles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(obstacles.getText())) {
					obstaclesError.setText("Please enter a valid integer");
					obstacles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else {
					int i = Integer.parseInt(obstacles.getText());
					if (i < 0 || i > 5) {
						obstaclesError.setText("Please enter an integer between 0 and 5");
						obstacles.pseudoClassStateChanged(errorClass, true);
						buttonClicked = cancel;
						event.consume();
					}
				}

				if (cycles.getText().equalsIgnoreCase("")) {
					cyclesError.setText("Please enter a cycles value");
					cycles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(cycles.getText())) {
					cyclesError.setText("Please enter a valid integer");
					cycles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}
			}

		});

		settingsDialog.setResultConverter(new Callback<ButtonType, Settings>() {

			@Override
			public Settings call(ButtonType b) {

				buttonClicked = b;

				if (b == ok) {

					if (!width.getText().equalsIgnoreCase("") && !height.getText().equalsIgnoreCase("")
							&& !food.getText().equalsIgnoreCase("") && !obstacles.getText().equalsIgnoreCase("")
							&& !cycles.getText().equalsIgnoreCase("")) {

						RadioButton selected = (RadioButton) iterations.getSelectedToggle();
						boolean displayIterations = false;

						if (selected.getText().equalsIgnoreCase("On")) {
							displayIterations = true;
						}

						return new Settings("", width.getText(), height.getText(), food.getText(), obstacles.getText(),
								cycles.getText(), displayIterations);
					} else {
						return new Settings("", "", "", "", "", "", true);
					}

				} else {
					return null;
				}
			}
		});

		Optional<Settings> result = settingsDialog.showAndWait();

		if (buttonClicked != cancel && result.isPresent()) {
			config.setProperty("mapWidth", result.get().width.replaceFirst("^0+(?!$)", ""));
			config.setProperty("mapHeight", result.get().height.replaceFirst("^0+(?!$)", ""));
			double fDensity = Double.parseDouble(result.get().food);
			fDensity /= 10;
			config.setProperty("foodDensity", String.valueOf(fDensity).replaceFirst("^0+(?!$)", ""));
			double oDensity = Double.parseDouble(result.get().obstacles);
			oDensity /= 10;
			config.setProperty("objDensity", String.valueOf(oDensity).replaceFirst("^0+(?!$)", ""));
			config.setProperty("cycles", result.get().cycles.replaceFirst("^0+(?!$)", ""));
			config.setProperty("displayIterations", String.valueOf(result.get().iterations));

			config.save();

			saved = true;

			buttonClicked = null;

		}

		return result;
	}

	private Optional<Settings> showConfigurationDialog(boolean fill) {
		Dialog<Settings> settingsDialog = new Dialog<>();

		DialogPane dPane = settingsDialog.getDialogPane();
		dPane.getStylesheets().add("file:resources/css/dialog.css");
		final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

		Stage configStage = (Stage) dPane.getScene().getWindow();
		configStage.getIcons().add(new Image("file:resources/images/configuration.png"));

		if (fill) {
			settingsDialog.setTitle("Edit Configuration");
			settingsDialog.setHeaderText("Editing configuration \"" + config.getFileName(false) + "\"");
		} else {
			settingsDialog.setTitle("New Configuration");
			settingsDialog.setHeaderText("Creating a new configuration");
		}
		settingsDialog.setGraphic(new ImageView(new Image("file:resources/images/configuration.png")));

		String buttonText = "";
		if (fill) {
			buttonText = "Ok";
		} else {
			buttonText = "Create";
		}

		final ButtonType ok = new ButtonType(buttonText, ButtonData.OK_DONE);
		final ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		settingsDialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 10, 10, 10));

		TextField name = new TextField();
		name.setPromptText("Enter a name");
		TextField width = new TextField();
		TextField height = new TextField();
		TextField food = new TextField();
		food.setPromptText("(0 - 5)");
		TextField obstacles = new TextField();
		obstacles.setPromptText("(0 - 5)");
		TextField cycles = new TextField();
		Label nameError = new Label(""), widthError = new Label(""), heightError = new Label(""),
				foodError = new Label(""), obstaclesError = new Label(""), cyclesError = new Label("");

		ToggleGroup iterations = new ToggleGroup();
		RadioButton on = new RadioButton("On");
		on.setToggleGroup(iterations);
		on.setSelected(true);
		RadioButton off = new RadioButton("Off");
		off.setToggleGroup(iterations);

		if (fill) {
			name.setText(config.getFileName(false));
			width.setText(config.getProperty("mapWidth"));
			height.setText(config.getProperty("mapHeight"));
			double num = Double.parseDouble(config.getProperty("foodDensity")) * 10;
			int density = (int) num;
			food.setText(String.valueOf(density));
			num = Double.parseDouble(config.getProperty("objDensity")) * 10;
			density = (int) num;
			obstacles.setText(String.valueOf(density));
			cycles.setText(config.getProperty("cycles"));
			Boolean displayIterations = Boolean.parseBoolean(config.getProperty("displayIterations"));
			on.setSelected(displayIterations);
			off.setSelected(!displayIterations);
		}

		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPrefWidth(300);

		if (fill) {
			grid.add(new Label("Configuration Name:"), 0, 0);
			grid.add(name, 1, 0);
			grid.add(nameError, 2, 0);
		}
		grid.add(new Label("Map Width:"), 0, 1);
		grid.add(width, 1, 1);
		grid.add(widthError, 2, 1);
		grid.add(new Label("Map Height:"), 0, 2);
		grid.add(height, 1, 2);
		grid.add(heightError, 2, 2);
		grid.add(new Label("Food Density:"), 0, 3);
		grid.add(food, 1, 3);
		grid.add(foodError, 2, 3);
		grid.add(new Label("Obstacle Density:"), 0, 4);
		grid.add(obstacles, 1, 4);
		grid.add(obstaclesError, 2, 4);
		grid.add(new Label("Number of Cycles:"), 0, 5);
		grid.add(cycles, 1, 5);
		grid.add(cyclesError, 2, 5);
		grid.add(new Label("Show iterations:"), 0, 6);
		grid.add(on, 1, 6);
		grid.add(off, 2, 6);

		grid.getColumnConstraints().addAll(col1, col2, col3);

		settingsDialog.getDialogPane().setContent(grid);

		if (fill) {
			Platform.runLater(() -> name.requestFocus());
		} else {
			Platform.runLater(() -> width.requestFocus());
		}

		if (fill) {
			name.textProperty().addListener((observable, oldValue, newValue) -> {
				if (oldValue.trim().equalsIgnoreCase("")) {
					name.pseudoClassStateChanged(errorClass, false);
					nameError.setText("");
				}
			});
		}

		width.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				width.pseudoClassStateChanged(errorClass, false);
				widthError.setText("");
			}
		});

		height.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				height.pseudoClassStateChanged(errorClass, false);
				heightError.setText("");
			}
		});

		food.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				food.pseudoClassStateChanged(errorClass, false);
				foodError.setText("");
			}
		});

		obstacles.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				obstacles.pseudoClassStateChanged(errorClass, false);
				obstaclesError.setText("");
			}
		});

		cycles.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				cycles.pseudoClassStateChanged(errorClass, false);
				cyclesError.setText("");
			}
		});

		settingsDialog.setOnCloseRequest((event) -> {

			if (buttonClicked == ok) {

				name.pseudoClassStateChanged(errorClass, false);
				width.pseudoClassStateChanged(errorClass, false);
				height.pseudoClassStateChanged(errorClass, false);
				food.pseudoClassStateChanged(errorClass, false);
				obstacles.pseudoClassStateChanged(errorClass, false);
				cycles.pseudoClassStateChanged(errorClass, false);
				nameError.setText("");
				widthError.setText("");
				heightError.setText("");
				foodError.setText("");
				obstaclesError.setText("");
				cyclesError.setText("");

				name.setText(name.getText().replaceAll("[^a-zA-Z0-9.-@]", ""));
				if (name.getText().trim().equalsIgnoreCase("") && fill) {
					nameError.setText("Please enter a configuration name");
					name.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else {

					if (fill) {
						if (!(config.getFileName(false).equalsIgnoreCase(name.getText()))
								&& new File(name.getText() + ".xml").exists()) {

							Optional<ButtonType> result = showAlert(AlertType.CONFIRMATION, "Overwrite file?", null,
									"Would you like to overwrite the existing file?");
							if (result.get() == ButtonType.CANCEL) {
								buttonClicked = cancel;
								event.consume();
							}
						}
					}

				}

				if (width.getText().equalsIgnoreCase("")) {
					widthError.setText("Please enter a width value");
					width.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(width.getText())) {
					widthError.setText("Please enter a valid integer");
					width.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (height.getText().equalsIgnoreCase("")) {
					heightError.setText("Please enter a height value");
					height.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(height.getText())) {
					heightError.setText("Please enter a valid integer");
					height.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (fill) {
					if (!width.getText().equalsIgnoreCase("") && isInteger(width.getText())
							&& !height.getText().equalsIgnoreCase("") && isInteger(height.getText())) {
						int widthValue = Integer.parseInt(width.getText());
						int heightValue = Integer.parseInt(height.getText());
						if (widthValue * heightValue < world.getLifeFormList().size()) {
							showAlert(AlertType.WARNING, "Insufficient Dimensions",
									"The current dimensions are too small",
									"The current width and height would create a map too small to hold all the current LifeForms.\nPlease either enter larger dimensions or remove some LifeForms");
							event.consume();
						}
					}
				}

				if (food.getText().equalsIgnoreCase("")) {
					foodError.setText("Please enter a food density value");
					food.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(food.getText())) {
					foodError.setText("Please enter a valid integer");
					food.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else {
					int i = Integer.parseInt(food.getText());
					if (i < 0 || i > 5) {
						foodError.setText("Please enter an integer between 0 and 5");
						food.pseudoClassStateChanged(errorClass, true);
						buttonClicked = cancel;
						event.consume();
					}
				}

				if (obstacles.getText().equalsIgnoreCase("")) {
					obstaclesError.setText("Please enter an obstacle density value");
					obstacles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(obstacles.getText())) {
					obstaclesError.setText("Please enter a valid integer");
					obstacles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else {
					int i = Integer.parseInt(obstacles.getText());
					if (i < 0 || i > 5) {
						obstaclesError.setText("Please enter an integer between 0 and 5");
						obstacles.pseudoClassStateChanged(errorClass, true);
						buttonClicked = cancel;
						event.consume();
					}
				}

				if (cycles.getText().equalsIgnoreCase("")) {
					cyclesError.setText("Please enter a cycles value");
					cycles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(cycles.getText())) {
					cyclesError.setText("Please enter a valid integer");
					cycles.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (!fill && buttonClicked != cancel) {
					FileChooser fc = new FileChooser();
					fc.setTitle("Save new configuration");
					fc.setInitialDirectory(config.getFile().getAbsoluteFile().getParentFile());
					fc.getExtensionFilters().add(new ExtensionFilter("Configuration files (*.xml)", "*.xml"));

					File newConfig = fc.showSaveDialog(dPane.getScene().getWindow());

					if (newConfig != null) {
						if (config.getExtension(newConfig).equalsIgnoreCase("xml")) {
							config.create(newConfig);
						} else {
							config.create(new File(newConfig.getAbsolutePath() + ".xml"));
						}
					} else {
						buttonClicked = cancel;
						event.consume();
					}
				}
			}

		});

		settingsDialog.setResultConverter(new Callback<ButtonType, Settings>() {

			@Override
			public Settings call(ButtonType b) {

				buttonClicked = b;

				if (b == ok) {

					if (!(fill && name.getText().equalsIgnoreCase("")) && !width.getText().equalsIgnoreCase("")
							&& !height.getText().equalsIgnoreCase("") && !food.getText().equalsIgnoreCase("")
							&& !obstacles.getText().equalsIgnoreCase("") && !cycles.getText().equalsIgnoreCase("")
							&& isInteger(width.getText()) && isInteger(height.getText())) {

						RadioButton selected = (RadioButton) iterations.getSelectedToggle();
						boolean displayIterations = false;

						if (selected.getText().equalsIgnoreCase("On")) {
							displayIterations = true;
						}

						return new Settings(name.getText(), width.getText(), height.getText(), food.getText(),
								obstacles.getText(), cycles.getText(), displayIterations);
					} else {
						return new Settings("", "", "", "", "", "", true);
					}

				} else {
					return null;
				}
			}
		});

		Optional<Settings> result = settingsDialog.showAndWait();

		if (buttonClicked != cancel && result.isPresent()) {
			if (fill) {
				if (!config.getFileName(false).equals(result.get().name)) {
					Path path = config.setName(result.get().name);
					if (path == null) {
						showAlert(AlertType.ERROR, "Renaming Failed",
								"The configuration file was not successfully renamed.",
								"All other settings have been saved to the file.");
					} else {
						config = new Configuration();
					}
				}

				Optional<ButtonType> resetResult = null;

				if (playing) {
					resetResult = showAlert(AlertType.CONFIRMATION, "Simulation Reset", "",
							"These changes will cause the simulation to stop and reset.\nDo you wish to continue?");
					if (resetResult.get().getButtonData() == ButtonData.OK_DONE) {
						timeline.stop();
						timeline = null;
						playing = false;
					}
				} else if (timeline != null) {
					timeline = null;
				}

				if (resetResult == null || resetResult.get().getButtonData() == ButtonData.OK_DONE) {
					double fDensity = Double.parseDouble(result.get().food);
					fDensity /= 10;
					double oDensity = Double.parseDouble(result.get().obstacles);
					oDensity /= 10;

					// Checks whether the width, height, food density or
					// obstacle
					// density have been changed.
					// If not program will skip regenerating the world and will
					// only
					// set the cycles and displayIterations properties.
					if (!result.get().width.replaceFirst("^0+(?!$)", "")
							.equalsIgnoreCase(config.getProperty("mapWidth"))
							|| !result.get().height.replaceFirst("^0+(?!$)", "")
									.equalsIgnoreCase(config.getProperty("mapHeight"))
							|| !String.valueOf(fDensity).replaceFirst("^0+(?!$)", "")
									.equalsIgnoreCase(config.getProperty("foodDensity"))
							|| !String.valueOf(oDensity).replaceFirst("^0+(?!$)", "")
									.equalsIgnoreCase(config.getProperty("objDensity"))) {

						// Regex expression removes leading zeros.
						// '^' matches zeros at start of string, '0+' matches 1
						// or more zeros
						// '(?!$) is a negative lookahead which prevents
						// matching entire string
						config.setProperty("mapWidth", result.get().width.replaceFirst("^0+(?!$)", ""));
						config.setProperty("mapHeight", result.get().height.replaceFirst("^0+(?!$)", ""));
						config.setProperty("foodDensity", String.valueOf(fDensity).replaceFirst("^0+(?!$)", ""));
						config.setProperty("objDensity", String.valueOf(oDensity).replaceFirst("^0+(?!$)", ""));
						config.setProperty("cycles", result.get().cycles.replaceFirst("^0+(?!$)", ""));
						config.setProperty("displayIterations", String.valueOf(result.get().iterations));
						config.save();

						clearSimPane();
						world.syncConfiguration();

						for (ALifeForm a : world.getLifeFormList()) {
							world.placeLifeForm(a);
						}
						world.placeFood(true);
						world.placeObstacles(true);
						world.save();

						if (worldContainers != null) {
							showWorld();
						}
					}
				} else {
					config.setProperty("cycles", result.get().cycles.replaceFirst("^0+(?!$)", ""));
					config.setProperty("displayIterations", String.valueOf(result.get().iterations));
					config.save();
				}

				buttonClicked = null;

			} else {
				double fDensity = Double.parseDouble(result.get().food);
				fDensity /= 10;
				double oDensity = Double.parseDouble(result.get().obstacles);
				oDensity /= 10;

				config.setProperty("mapWidth", result.get().width.replaceFirst("^0+(?!$)", ""));
				config.setProperty("mapHeight", result.get().height.replaceFirst("^0+(?!$)", ""));
				config.setProperty("foodDensity", String.valueOf(fDensity).replaceFirst("^0+(?!$)", ""));
				config.setProperty("objDensity", String.valueOf(oDensity).replaceFirst("^0+(?!$)", ""));
				config.setProperty("cycles", result.get().cycles.replaceFirst("^0+(?!$)", ""));
				config.setProperty("displayIterations", String.valueOf(result.get().iterations));
				config.save();

				world = new AWorld(config);
				world.placeFood(true);
				world.placeObstacles(true);
				world.save();
			}
		}

		return result;
	}

	private Optional<ALifeForm> showLifeFormDialog(ALifeForm lf) {
		Dialog<ALifeForm> lifeFormDialog = new Dialog<>();

		DialogPane dPane = lifeFormDialog.getDialogPane();
		dPane.getStylesheets().add("file:resources/css/dialog.css");
		final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

		Stage lifeFormStage = (Stage) dPane.getScene().getWindow();
		lifeFormStage.getIcons().add(new Image("file:resources/images/invader.png"));

		if (lf != null) {
			lifeFormDialog.setTitle("Modify LifeForm");
			lifeFormDialog.setHeaderText("Editing LifeForm #" + lf.getID() + " - " + lf.getName());
		} else {
			lifeFormDialog.setTitle("New LifeForm");
			lifeFormDialog.setHeaderText("Creating LifeForm #" + (world.getLifeFormList().size() + 1));
		}
		lifeFormDialog.setGraphic(new ImageView(new Image("file:resources/images/invader.png")));

		String buttonText = "";
		if (lf != null) {
			buttonText = "Ok";
		} else {
			buttonText = "Create";
		}

		final ButtonType ok = new ButtonType(buttonText, ButtonData.OK_DONE);
		final ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		lifeFormDialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 10, 10, 10));

		TextField name = new TextField();
		name.setPromptText("Enter a name");
		ComboBox<String> species = new ComboBox<String>();
		species.getItems().addAll("Herbivore", "Carnivore");
		TextField energy = new TextField();
		Label nameError = new Label(""), speciesError = new Label(""), energyError = new Label("");

		if (lf != null) {
			name.setText(lf.getName());
			species.setValue(lf.getSpecies());
			energy.setText(String.valueOf(lf.getEnergy()));
		}

		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPrefWidth(300);

		grid.add(new Label("LifeForm Name:"), 0, 0);
		grid.add(name, 1, 0);
		grid.add(nameError, 2, 0);
		grid.add(new Label("Species:"), 0, 1);
		grid.add(species, 1, 1);
		grid.add(speciesError, 2, 1);
		grid.add(new Label("Energy:"), 0, 3);
		grid.add(energy, 1, 3);
		grid.add(energyError, 2, 3);

		grid.getColumnConstraints().addAll(col1, col2, col3);

		lifeFormDialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> name.requestFocus());

		name.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				name.pseudoClassStateChanged(errorClass, false);
				nameError.setText("");
			}
		});

		species.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue == null) {
				species.pseudoClassStateChanged(errorClass, false);
				speciesError.setText("");
			}
		});

		energy.textProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue.trim().equalsIgnoreCase("")) {
				energy.pseudoClassStateChanged(errorClass, false);
				energyError.setText("");
			}
		});

		lifeFormDialog.setOnCloseRequest((event) -> {

			if (buttonClicked == ok) {

				name.pseudoClassStateChanged(errorClass, false);
				species.pseudoClassStateChanged(errorClass, false);
				energy.pseudoClassStateChanged(errorClass, false);
				nameError.setText("");
				speciesError.setText("");
				energyError.setText("");

				if (name.getText().trim().equalsIgnoreCase("")) {
					nameError.setText("Please enter a configuration name");
					name.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (species.getValue() == null) {
					speciesError.setText("Please select a species");
					species.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}

				if (energy.getText().equalsIgnoreCase("")) {
					energyError.setText("Please enter a food density value");
					energy.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				} else if (!isInteger(energy.getText())) {
					energyError.setText("Please enter a valid integer");
					energy.pseudoClassStateChanged(errorClass, true);
					buttonClicked = cancel;
					event.consume();
				}
			}

		});

		lifeFormDialog.setResultConverter(new Callback<ButtonType, ALifeForm>() {

			@Override
			public ALifeForm call(ButtonType b) {

				buttonClicked = b;

				if (b == ok) {

					if (!name.getText().equalsIgnoreCase("") && !species.getValue().equalsIgnoreCase("")
							&& !energy.getText().equalsIgnoreCase("") && isInteger(energy.getText())) {

						int id;

						if (lf != null) {
							id = lf.getID();
						} else {
							id = world.getLifeFormList().size() + 1;
						}

						if (species.getValue().equalsIgnoreCase("Herbivore")) {
							return new AHerbivore(world, name.getText(), Integer.parseInt(energy.getText()), id);
						} else {
							return new ACarnivore(world, name.getText(), Integer.parseInt(energy.getText()), id);
						}

					} else {
						return new AHerbivore(world, "", 0, 0);
					}
				} else {
					return null;
				}
			}
		});

		Optional<ALifeForm> result = lifeFormDialog.showAndWait();

		if (buttonClicked != cancel && result.isPresent()) {
			if (lf != null) {
				result.get().setY(lf.getY());
				result.get().setX(lf.getX());
				world.getLifeFormList().set(lf.getID() - 1, result.get());
				world.setMapValue(null, lf.getY(), lf.getX());
			} else {
				world.getLifeFormList().add(result.get());
			}
			world.placeLifeForm(result.get());
			if (worldContainers != null
					&& !worldContainers[result.get().getY()][result.get().getX()].getChildren().isEmpty()) {
				worldContainers[result.get().getY()][result.get().getX()].getChildren().remove(0);
				worldContainers[result.get().getY()][result.get().getX()].getChildren()
						.add((new ImageView(new Image(result.get().getSymbol()))));
			}
			world.save();
		}

		return result;

	}

	/**
	 * @author Jed Brennen
	 *
	 *         Class which stores the configuration values for the simulation.
	 */
	private class Settings {

		private String name, width, height, food, obstacles, cycles;
		private boolean iterations;

		Settings(String name, String width, String height, String food, String obstacles, String cycles,
				boolean iterations) {
			this.name = name;
			this.width = width;
			this.height = height;
			this.food = food;
			this.obstacles = obstacles;
			this.cycles = cycles;
			this.iterations = iterations;
		}

	}

	/**
	 * Checks whether the given string represents an integer value. Will return
	 * false if any character in the string is not an integer representation.
	 * 
	 * @param input
	 * @return true if the parameter represents an integer value
	 */
	public boolean isInteger(String input) {
		if (input == null || input.length() == 0) {
			return false;
		}

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
