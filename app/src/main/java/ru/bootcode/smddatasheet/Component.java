package ru.bootcode.smddatasheet;

/*
 * Created by Stepchenkov Sergey on 25.07.2018.
 * Класс описывает структу Компонента
 *
 * Немного избыточен, надо бы почистить после ближайщих тестов
 */

public class Component {
    private int _id;                    // идентификатор записи в базе данных
    private String _name;               // Наименование Компонента
    private String _body;               // Кодировка корпуса
    private String _label;               // Маркировка наносимая на SMD
    private String _prod;               // Производитель
    private String _func;               // Описание (Назначение - Транзистор, Диод...)
    private String _datasheet;          // Ссылка(имя файла PDF) на даташит расположенного на сервере

    private int _forvarite;
    private int _islcal;
    private int _image;                 // Картинка - Зарезервировано

    public Component(int id, String name, String body, String label, String prod, String func, String datasheet){
        this._id = id;
        this._name = name;
        this._body = body;
        this._label = label;
        this._prod = prod;
        this._func = func;
        this._datasheet = datasheet;
        int _image=0;
    }

    public Component(int id, String body, String label, String func){
        this._id = id;
        this._name = "";
        this._body = body;
        this._label = label;
        this._prod = "";
        this._func = func;
        this._datasheet = "";
        int _image=0;
    }

    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public void setBody(String body){
        this._body = body;
    }

    public void setName(String name){
        this._name = name;
    }

    public void setLabel(String label){
        this._label = label;
    }

    public void setProd(String prod){
        this._prod = prod;
    }

    public void setFunc(String func){
        this._func = func;
    }

    public void setDatasheet(String datasheet){
        this._datasheet = datasheet;
    }

    public String getName(){
        return this._name;
    }

    public String getBody(){
        return this._body;
    }

    public String getLabel(){
        return this._label;
    }

    public String getProd(){
        return this._prod;
    }

    public String getFunc(){
        return this._func;
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
