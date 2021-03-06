package plagamedicum.ppvis.lab2s4.Controller;

import javafx.collections.ObservableList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import plagamedicum.ppvis.lab2s4.model.FrazeChecker;
import plagamedicum.ppvis.lab2s4.model.snpOfVeterinarian;
import plagamedicum.ppvis.lab2s4.model.Pet;

public class Controller {
    private Pet model;
    private FrazeChecker frazeChecker;
    private DocOpener docOpener;
    private int rowsOnPage;
    private int currentPage = 1;
    private int numberOfPages;
    private String pagination;
    private String itemsCount;

    public Controller(Pet model){
        this.model = model;
    }

    public List<Pet> getPetList(){
        return model.getPetList();
    }

    public void addPet(String petName, LocalDate petBirthday, LocalDate petLast, String surname, String name, String patronym, String diagnosis){
        model.addPet(new Pet(petName, petBirthday, petLast, new snpOfVeterinarian(surname, name, patronym), diagnosis));
    }

    public void openDoc(File file) {
        docOpener = new DocOpener();
        try {
            model.setPetList(docOpener.openDoc(file));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveDoc(File file) {
        List<Pet> petList = model.getPetList();
        Element pets;
        Element pet;
        Element petName;
        Element petBirthday;
        Element petLastAppointment;
        Element snp;
        Element diagnosis;
        Attr petNameName;
        Attr petBirthdayName;
        Attr petLastAppointmentName;
        Attr surname;
        Attr name;
        Attr patronym;
        Attr diagnosisName;
       // Запарсили XML, создав структуру Document
        Document doc;
        DocumentBuilderFactory docBuilderFactory;
        DocumentBuilder docBuilder;
        TransformerFactory transformerFactory;
        Transformer transformer;
        DOMSource source;
        StreamResult streamResult;

        try{
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();

            pets = doc.createElement("pets");
            doc.appendChild(pets);

            for (Pet petI : petList){
                petName = doc.createElement("petName");
                petNameName = doc.createAttribute("name");
                petNameName.setValue(petI.getPetName());
                petName.setAttributeNode(petNameName);

                petBirthday = doc.createElement("petBirthday");
                petBirthdayName = doc.createAttribute("name");
                petBirthdayName.setValue(petI.getPetBirthdayString());
                petBirthday.setAttributeNode(petBirthdayName);

                petLastAppointment = doc.createElement("petLastAppointment");
                petLastAppointmentName = doc.createAttribute("name");
                petLastAppointmentName.setValue(petI.getPetLastAppointmentString());
                petLastAppointment.setAttributeNode(petLastAppointmentName);

                surname = doc.createAttribute("surname");
                surname.setValue(petI.getSnp().getSurname());
                name = doc.createAttribute("name");
                name.setValue(petI.getSnp().getName());
                patronym = doc.createAttribute("patronym");
                patronym.setValue(petI.getSnp().getPatronym());
                snp = doc.createElement("snp");
                snp.setAttributeNode(surname);
                snp.setAttributeNode(name);
                snp.setAttributeNode(patronym);

                diagnosis = doc.createElement("diagnosis");
                diagnosisName = doc.createAttribute("name");
                diagnosisName.setValue(petI.getDiagnosis());
                diagnosis.setAttributeNode(diagnosisName);

                pet = doc.createElement("pet");
                pet.appendChild(petName);
                pet.appendChild(petBirthday);
                pet.appendChild(petLastAppointment);
                pet.appendChild(snp);
                pet.appendChild(diagnosis);
                pets.appendChild(pet);
            }

            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            source = new DOMSource(doc);
            streamResult = new StreamResult(file);
            transformer.transform(source, streamResult);
        } catch (Exception  exception){
            exception.printStackTrace();
        }
    }
//Поиск
    public List<Pet> search(String selectedItem, List<String> criteriaListText, List<LocalDate> criteriaListDate){
        List<Pet> petList = getPetList();
        List<Pet> resultList;
        resultList = new ArrayList<>();
        SearchCriteria criteriaForSelection = SearchCriteria.getCriteriaByName(selectedItem);
        switch (criteriaForSelection){
            case LAST_DATE_AND_SNP_CRITERIA:
                final LocalDate LAST_APPOINTMENT = criteriaListDate.get(0);
                final String SURNAME = criteriaListText.get(0);
                final String NAME = criteriaListText.get(1);
                final String PATRONYM = criteriaListText.get(2);
                for(Pet pet:petList) {
                    if (pet.getSurname().equals(SURNAME) && pet.getName().equals(NAME) && pet.getPatronym().equals(PATRONYM) && pet.getPetLastAppointment().equals(LAST_APPOINTMENT)) {
                        resultList.add(pet);
                    }
                }
                break;
            case PET_NAME_AND_BIRTHDAY_CRITERIA:
                final LocalDate BIRTHDAY = criteriaListDate.get(1);
                final String PET_NAME = criteriaListText.get(3);
                for(Pet pet:petList) {
                    if (pet.getPetName().equals(PET_NAME) && pet.getPetBirthday().equals(BIRTHDAY)) {
                        resultList.add(pet);
                    }
                }
                break;
            case FRAZE_FROM_DIAGNOSIS_CRITERIA:
                frazeChecker = new FrazeChecker();
                final String  FRAZE   = criteriaListText.get(4);
                for(Pet pet:petList) {
                    if (frazeChecker.checkFrazeFromDiagnosis(pet.getDiagnosis(), FRAZE)) {
                        resultList.add(pet);
                    }
                }
                break;
        }

        return resultList;
    }

//удоление
    public void delete(List<Pet> indexList){
        for(Pet pet:indexList){
            getPetList().remove(pet);
        }
    }

    public void setRowsOnPage(String rowText, ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        rowsOnPage = Integer.parseInt(rowText);
        currentPage = 1;

        refreshPage(petObsList, curPetObsList);
    }

    public void goBegin(ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        currentPage = 1;
        refreshPage(petObsList, curPetObsList);
    }

    public void goLeft(ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        if(currentPage > 1){
            currentPage--;
        }
        refreshPage(petObsList, curPetObsList);
    }

    public void goRight(ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        if(currentPage < numberOfPages){
            currentPage++;
        }
        refreshPage(petObsList, curPetObsList);
    }

    public void goEnd(ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        currentPage = numberOfPages;
        refreshPage(petObsList, curPetObsList);
    }

    private void refreshPage(ObservableList<Pet> petObsList, ObservableList<Pet> curPetObsList){
        int fromIndex = (currentPage - 1) * rowsOnPage;
        int toIndex   =  currentPage * rowsOnPage;

        if(toIndex > petObsList.size()){
            toIndex = petObsList.size();
        }

        curPetObsList.clear();
        curPetObsList.addAll(
                petObsList.subList(
                        fromIndex,
                        toIndex
                )
        );

        refreshPagination(petObsList);
    }

    private void refreshPagination(ObservableList<Pet> petObsList){
        numberOfPages = (petObsList.size() - 1) / rowsOnPage + 1;
        pagination = currentPage + "/" + numberOfPages;
        itemsCount = "/" + petObsList.size() + "/";
    }

    public String getPagination() {
        return pagination;
    }

}

