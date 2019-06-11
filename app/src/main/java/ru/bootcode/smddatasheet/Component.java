package ru.bootcode.smddatasheet;

/**
 * Created by Администратор on 25.07.2018.
 * Класс описывает структу Компонента
 */

public class Component {
    int _id;                    // идентификатор записи в базе данных
    String _name;               // Наименование Компонента
    String _code;               // Кодировка корпуса
    String _marker;             // Маркировка наносимая на SMD
    String _prod;               // Производитель
    String _note;               // Описание (Назначение - Транзистор, Диод...)
    String _datasheet;          // Ссылка(имя файла PDF) на даташит расположенного на сервере

    int _forvarite;
    int _islcal;
    int _image;                 // Картинка - Зарезервировано

    public Component(){
        int _image=0;
    }

    public Component(int id, String name, String code, String marker, String prod, String note, String datasheet){
        this._id = id;
        this._name = name;
        this._code = code;
        this._marker = marker;
        this._prod = prod;
        this._note = note;
        this._datasheet = datasheet;
        int _image=0;
    }

    public Component(int id, String code, String marker, String note){
        this._id = id;
        this._name = "";
        this._code = code;
        this._marker = marker;
        this._prod = "";
        this._note = note;
        this._datasheet = "";
        int _image=0;
    }

    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public void setCode(String code){
        this._code = code;
    }

    public void setName(String name){
        this._name = name;
    }

    public void setMarker(String marker){
        this._marker = marker;
    }

    public void setProd(String prod){
        this._prod = prod;
    }

    public void setNote(String note){
        this._note = note;
    }

    public void setDatasheet(String datasheet){
        this._datasheet = datasheet;
    }

    public String getName(){
        return this._name;
    }

    public String getCode(){
        return this._code;
    }

    public String getMarker(){
        return this._marker;
    }

    public String getProd(){
        return this._prod;
    }

    public String getNote(){
        return this._note;
    }

    public String getDatasheet(){
        return this._datasheet;
    }

    public int getImage(){
        return this._image;
    }


    public int get_forvarite() {
        return _forvarite;
    }

    public void set_forvarite(int _forvarite) {
        this._forvarite = _forvarite;
    }

    public int get_islcal() {
        return _islcal;
    }

    public void set_islcal(int _islcal) {
        this._islcal = _islcal;
    }

}
