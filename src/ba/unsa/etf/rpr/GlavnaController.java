package ba.unsa.etf.rpr;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sun.nio.ch.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GlavnaController {

    public GeografijaDAO dao;
    GradController gradController;
    DrzavaController drzavaController;

    public TableView <Grad> tableViewGradovi;
    public TableColumn<Grad, Integer> colGradId;
    public TableColumn<Grad, String> colGradNaziv;
    public TableColumn<Grad, Integer> colGradStanovnika;
    public TableColumn<Grad, String> colGradDrzava;
    public TableColumn<Grad, String> colGradSlika;
    public TableColumn<Grad, Integer> colGradPostanskiBroj;
    public Button btnDodajGrad;
    public Button btnDodajDrzavu;
    public Button btnIzmijeniGrad;
    public Button btnObrisiGrad;
    private ObservableList<Grad> listaZaTabelu;

    public GlavnaController (GeografijaDAO d) {
        dao = d;
    }

    public GlavnaController() {
        dao = GeografijaDAO.getInstance();
        listaZaTabelu = dao.getListaGradova();
    }


    @FXML
    public void initialize () {
        ObservableList<Grad> listaGradova = FXCollections.observableArrayList(dao.gradovi());
        tableViewGradovi.setItems(listaGradova);
        colGradId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGradNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colGradStanovnika.setCellValueFactory(new PropertyValueFactory<>("brojStanovnika"));
        colGradDrzava.setCellValueFactory(new PropertyValueFactory<>("drzava"));
        colGradSlika.setCellValueFactory(new PropertyValueFactory<>("slika"));
        colGradPostanskiBroj.setCellValueFactory(new PropertyValueFactory<>("postanskiBroj"));
        tableViewGradovi.getColumns().setAll(colGradId, colGradNaziv, colGradStanovnika, colGradDrzava, colGradSlika, colGradPostanskiBroj);
    }

    public void resetujBazu() {
        GeografijaDAO.removeInstance();
        File dbfile = new File("baza.db");
        dbfile.delete();
        dao = GeografijaDAO.getInstance();
    }


    public void dodajDrzavu(ActionEvent actionEvent) throws IOException {
        Parent root = null;
        Stage myStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/drzava.fxml"));
        loader.setController(new DrzavaController(null, dao.gradovi()));
        drzavaController = loader.getController();
        root = loader.load();
        myStage.setTitle("Drzave");

        myStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        myStage.show();

        myStage.setOnHiding(event -> dao.dodajDrzavu(drzavaController.getDrzava()));
        //Dio koda zamijenjen labmda funkcijom
            myStage.setOnHiding(event -> Platform.runLater(() -> {
                if (drzavaController.getDrzava() != null)
                    dao.dodajDrzavu(drzavaController.getDrzava());
            }));
    }

    public void dodajGrad (ActionEvent actionEvent) throws IOException, SQLException {
        Parent root = null;
        Stage myStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"));
        loader.setController(new GradController(null, dao.drzave()));
        gradController = loader.getController();
        root = loader.load();
        myStage.setTitle("Gradovi");

        myStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        myStage.show();
        myStage.setOnHiding(event -> Platform.runLater(() -> {
            if (gradController.getGrad() != null)
                dao.dodajGrad(gradController.getGrad());
            listaZaTabelu.setAll(dao.getListaGradova());
        }));

    }

    public void promijeniGrad (ActionEvent actionEvent) throws IOException {

        Grad trenutniGrad = tableViewGradovi.getSelectionModel().getSelectedItem();
        if (trenutniGrad == null)
            return;

        Parent root = null;
        Stage myStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/grad.fxml"));
        loader.setController(new GradController(trenutniGrad, dao.drzave())); //staviti grad koji se edituje umjesto null
        gradController = loader.getController();
        root = loader.load();
        myStage.setTitle("Gradovi");
        gradController = loader.getController();

        myStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        myStage.show();
//        //Dio koda koji je zamjenjen lambda funkcijom:
        myStage.setOnHiding(event -> Platform.runLater(() -> {
            if (gradController.getGrad() != null)
                dao.izmijeniGrad(gradController.getGrad());
            listaZaTabelu.setAll(dao.gradovi());
        }));
    }

    public void obrisiGrad (ActionEvent actionEvent) throws IOException {
        Grad trenutniGrad = tableViewGradovi.getSelectionModel().getSelectedItem();

        if (trenutniGrad == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setContentText("Are you sure you want to delete: " + trenutniGrad.getNaziv() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
            dao.obrisiGrad(trenutniGrad);
        listaZaTabelu.setAll(dao.getListaGradova());
    }

}
