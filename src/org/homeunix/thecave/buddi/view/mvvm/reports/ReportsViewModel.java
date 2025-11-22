package org.homeunix.thecave.buddi.view.mvvm.reports;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.plugin.BuddiPluginFactory;
import org.homeunix.thecave.buddi.plugin.api.BuddiReportPlugin;
import org.homeunix.thecave.buddi.plugin.api.model.impl.ImmutableDocumentImpl;
import org.homeunix.thecave.buddi.plugin.api.util.HtmlPage;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportsViewModel extends ViewModel {

    private final Document document;
    private final ObservableList<BuddiReportPlugin> availableReports = FXCollections.observableArrayList();
    private final ObjectProperty<BuddiReportPlugin> selectedReport = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now().minusMonths(1));
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty reportHtml = new SimpleStringProperty("");

    public ReportsViewModel(Document document) {
        this.document = document;
        loadReports();
    }

    @SuppressWarnings("unchecked")
    private void loadReports() {
        List<BuddiReportPlugin> plugins = (List<BuddiReportPlugin>) BuddiPluginFactory
                .getPlugins(BuddiReportPlugin.class);
        availableReports.setAll(plugins);
        if (!availableReports.isEmpty()) {
            selectedReport.set(availableReports.get(0));
        }
    }

    public void generateReport() {
        BuddiReportPlugin report = selectedReport.get();
        if (report != null && document != null) {
            try {
                Date start = Date.from(startDate.get().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(endDate.get().atStartOfDay(ZoneId.systemDefault()).toInstant());

                // Reports expect ImmutableDocument
                HtmlPage page = report.getReport(new ImmutableDocumentImpl(document), null, start, end);

                String html = processHtml(page);
                reportHtml.set(html);

            } catch (Exception e) {
                e.printStackTrace();
                reportHtml
                        .set("<html><body><h1>Error generating report</h1><p>" + e.getMessage() + "</p></body></html>");
            }
        }
    }

    private String processHtml(HtmlPage page) {
        String html = page.getHtml();
        Map<String, BufferedImage> images = page.getImages();

        if (images != null) {
            for (Map.Entry<String, BufferedImage> entry : images.entrySet()) {
                String imageName = entry.getKey();
                BufferedImage image = entry.getValue();
                String base64 = encodeImageToBase64(image);

                // Replace image references with Base64 data URIs
                // Assuming images are referenced like <img src='imageName'> or similar
                // We need to be careful with replacement.
                // HtmlPage usually returns HTML where images are expected to be saved to disk.
                // We will try to replace "src='" + imageName + "'" with
                // "src='data:image/png;base64," + base64 + "'"

                html = html.replace("src=\"" + imageName + "\"", "src=\"data:image/png;base64," + base64 + "\"");
                html = html.replace("src='" + imageName + "'", "src='data:image/png;base64," + base64 + "'");
            }
        }
        return html;
    }

    private String encodeImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public ObservableList<BuddiReportPlugin> getAvailableReports() {
        return availableReports;
    }

    public ObjectProperty<BuddiReportPlugin> selectedReportProperty() {
        return selectedReport;
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public StringProperty reportHtmlProperty() {
        return reportHtml;
    }
}
