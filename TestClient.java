public class TestClient {
    public static void main(String[] args) throws Exception {
        URL wsdlURL = new URL("http://localhost:8080/myapp/CalculatorService?wsdl");
        QName SERVICE_NAME = new QName("http://mio.package/", "CalculatorService");
        CalculatorService service = new CalculatorService(wsdlURL, SERVICE_NAME);
        CalculatorServicePortType port = service.getCalculatorServicePort(); // ottiene il port (proxy)
        int risultato = port.somma(5, 7);
        System.out.println("Somma(5,7) = " + risultato);
        try {
            double risultato2 = port.dividi(10.0, 0.0);
            System.out.println("Dividi(10,0) = " + risultato2);
        } catch (DivisionePerZeroException e) {
            System.err.println("Errore dal servizio: " + e.getMessage());
        }
    }
}