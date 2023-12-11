package controllers;

import entities.Employe;
import controllers.util.JsfUtil;
import controllers.util.JsfUtil.PersistAction;
import entities.Machine;
import services.EmployeFacade;
import services.MachineFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.BarChartSeries;

@ManagedBean(name = "employeController")
@SessionScoped
public class EmployeController implements Serializable {

    @EJB
    private services.EmployeFacade ejbFacade;
    private List<Employe> items = null;
    private Employe selected;
    private List<Machine> machinesAttribuees;
     

    @EJB
    private MachineFacade machineFacade;
    private BarChartModel chartModel;
        
        public BarChartModel getChartModel() {
    return chartModel;
}

public void setChartModel(BarChartModel chartModel) {
    this.chartModel = chartModel;
}

    
   


public List<Machine> getMachinesAttribuees() {
    return machinesAttribuees;
}

public void setMachinesAttribuees(List<Machine> machinesAttribuees) {
    this.machinesAttribuees = machinesAttribuees;
}


    public EmployeController() {
           // createChartModel(); // Ajoutez cet appel pour initialiser le chartModel

    }

    public Employe getSelected() {
        return selected;
    }

    public void setSelected(Employe selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private EmployeFacade getFacade() {
        return ejbFacade;
    }

    public Employe prepareCreate() {
        selected = new Employe();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("EmployeCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("EmployeUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("EmployeDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Employe> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public List<Employe> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Employe> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Employe.class)
    public static class EmployeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EmployeController controller = (EmployeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "employeController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Employe) {
                Employe o = (Employe) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Employe.class.getName()});
                return null;
            }
        }

    }
    
  public void createChartModel() {
    chartModel = new BarChartModel();

    BarChartSeries series = new BarChartSeries();
    series.setLabel("Machines Acquises par Année");

    // Récupérez toutes les machines de la base de données
    List<Machine> allMachines = machineFacade.findAll();

    // Mettez à jour les données à partir de toutes les machines
    if (allMachines != null) {
        for (Machine machine : allMachines) {
            String annee = String.valueOf(machine.getDateAchat());
            Number currentCount = series.getData().get(annee);
            series.set(annee, currentCount != null ? currentCount.intValue() + 1 : 1);
        }
    }

    chartModel.addSeries(series);

    // Configurer les axes du graphique
    Axis xAxis = chartModel.getAxis(AxisType.X);
    xAxis.setLabel("Année");

    Axis yAxis = chartModel.getAxis(AxisType.Y);
    yAxis.setLabel("Nombre de Machines");
}


    
  public void chargerMachinesAttribuees() {
    if (selected != null) {
        System.out.println("Chargement des machines attribuées pour l'employé : " + selected.getId());
        machinesAttribuees = machineFacade.getMachinesAttribuees(selected.getId());
        System.out.println("Nombre de machines attribuées : " + machinesAttribuees.size());
    } else {
        System.out.println("Aucun employé sélectionné. Vérifiez que 'selected' est correctement initialisé.");
        machinesAttribuees = null;
    }
        createChartModel(); // Assurez-vous que createChartModel est appelé après la mise à jour de machinesAttribuees

}
    
    

}
