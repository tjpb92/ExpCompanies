package expcompanies;

import bkgpi2a.ClientCompany;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PaperSize;
import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

/**
 * Programme pour exporter les sociétés d'un site Web dans un fichier Excel
 *
 * @author Thierry Baribaud
 * @version 0.02
 */
public class ExpCompanies {

    private final static String path = "c:\\temp";

    private final static String filename = "companies.xlsx";

    private final static String HOST = "10.65.62.133";
    private final static int PORT = 27017;

    /**
     * @param args arguments en ligne de commande
     */
    public static void main(String[] args) {

        FileOutputStream out;
        XSSFWorkbook classeur;
        XSSFSheet feuille;
        XSSFRow titre;
        XSSFCell cell;
        XSSFRow ligne;
        XSSFCellStyle cellStyle;
        XSSFCellStyle titleStyle;
        ObjectMapper objectMapper;
        ClientCompany clientCompany;
        MongoDatabase mongoDatabase;
        MongoClient MyMongoClient;
        CreationHelper createHelper;
        XSSFHyperlink link;
        XSSFCellStyle hlinkStyle;
        XSSFFont hlinkFont;

        objectMapper = new ObjectMapper();

        MyMongoClient = new MongoClient(HOST, PORT);
//        mongoDatabase = MyMongoClient.getDatabase("extranet");
        mongoDatabase = MyMongoClient.getDatabase("extranet-dev");

        MongoCollection<Document> MyCollection = mongoDatabase.getCollection("clientCompanies");
        System.out.println(MyCollection.count() + " sociétés");

//      Création d'un classeur Excel
        classeur = new XSSFWorkbook();
        createHelper = classeur.getCreationHelper();
        feuille = classeur.createSheet("Sociétés");

        // Style de cellule avec bordure noire
        cellStyle = classeur.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

        // Style pour le titre
        titleStyle = (XSSFCellStyle) cellStyle.clone();
        titleStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        titleStyle.setFillPattern(FillPatternType.LESS_DOTS);
//        titleStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());

        // Style pour les liens dans les cellules
        hlinkStyle = (XSSFCellStyle) cellStyle.clone();
        hlinkFont = classeur.createFont();
        hlinkFont.setUnderline(XSSFFont.U_SINGLE);
        hlinkFont.setColor(HSSFColor.BLUE.index);
        hlinkStyle.setFont(hlinkFont);

        // Ligne de titre
        titre = feuille.createRow(0);
        cell = titre.createCell((short) 0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("Nom");
        cell = titre.createCell((short) 1);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("Etat");
        cell = titre.createCell((short) 2);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("Type");
        cell = titre.createCell((short) 3);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("ID Anstel");
        cell = titre.createCell((short) 4);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("ID Performance Immo");
//        cell = titre.createCell((short) 5);
//        cell.setCellStyle(titleStyle);
//        cell.setCellValue("Société");
        cell = titre.createCell((short) 5);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("Numéro Siret");
        cell = titre.createCell((short) 6);
        cell.setCellStyle(titleStyle);
        cell.setCellValue("Mode de facturation");

        // Lit les sociétés classés par nom
        MongoCursor<Document> MyCursor
                = MyCollection.find().sort(new BasicDBObject("label", 1)).iterator();
        int n = 0;
        try {
            while (MyCursor.hasNext()) {
                clientCompany = objectMapper.readValue(MyCursor.next().toJson(), ClientCompany.class);
                System.out.println(n
                        + " label:" + clientCompany.getLabel()
                        + ", isactive:" + clientCompany.getIsActive()
                        + ", companyType:" + clientCompany.getCompanyType()
                        + ", id:" + clientCompany.getId()
                        + ", uid:" + clientCompany.getUid());
                n++;
                ligne = feuille.createRow(n);

                cell = ligne.createCell(0);
                cell.setCellValue(clientCompany.getLabel());
                cell.setCellStyle(cellStyle);

                cell = ligne.createCell(1);
                if (clientCompany.getIsActive()) {
                    cell.setCellValue("Actif");
                } else {
                    cell.setCellValue("Inactif");
                }
                cell.setCellStyle(cellStyle);

                cell = ligne.createCell(2);
                cell.setCellValue(clientCompany.getCompanyType());
                cell.setCellStyle(cellStyle);

                cell = ligne.createCell(3);
                cell.setCellValue(clientCompany.getId());
                cell.setCellStyle(cellStyle);

                cell = ligne.createCell(4);
                cell.setCellValue(clientCompany.getUid());
                link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress("https://dashboard.performance-immo.com/clientCompanies/" + clientCompany.getUid());
                link.setLabel(clientCompany.getUid());
                cell.setHyperlink((XSSFHyperlink) link);
                cell.setCellStyle(hlinkStyle);

                cell = ligne.createCell(5);
                cell.setCellValue(clientCompany.getSiretNumber());
                cell.setCellStyle(cellStyle);

                cell = ligne.createCell(6);
                cell.setCellValue(clientCompany.getBillingType());
                cell.setCellStyle(cellStyle);
            }

            // Ajustement automatique de la largeur des colonnes
            for (int k = 0; k < 7; k++) {
                feuille.autoSizeColumn(k);
            }

            // Format A4 en sortie
            feuille.getPrintSetup().setPaperSize(PaperSize.A4_PAPER);

            // Orientation paysage
            feuille.getPrintSetup().setLandscape(true);

            // Ajustement à une page en largeur
            feuille.setFitToPage(true);
            feuille.getPrintSetup().setFitWidth((short) 1);
            feuille.getPrintSetup().setFitHeight((short) 0);

            // En-tête et pied de page
            Header header = feuille.getHeader();
            header.setLeft("Liste des sociétés Extranet Anstel");
            header.setRight("&F");

            Footer footer = feuille.getFooter();
            footer.setLeft("Documentation confidentielle Anstel");
            footer.setCenter("Page &P / &N");
            footer.setRight("&D");

            // Ligne à répéter en haut de page
            feuille.setRepeatingRows(CellRangeAddress.valueOf("1:1"));

        } catch (IOException ex) {
            Logger.getLogger(ExpCompanies.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MyCursor.close();
        }

        // Enregistrement du classeur dans un fichier
        try {
            out = new FileOutputStream(new File(path + "\\" + filename));
            classeur.write(out);
            out.close();
            System.out.println("Fichier Excel " + filename + " créé dans " + path);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExpCompanies.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExpCompanies.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
