import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConversorDeMoedas {
    private static final String CHAVE_API = "e79ff49287183a45beecc66d"; // Minha chave da API, risco.
    private Map<String, Double> taxas;
    private Scanner entrada;

    public ConversorDeMoedas() {
        this.entrada = new Scanner(System.in);
        this.taxas = buscarTaxasDeCambio();
    }

    public void iniciar() {
        while (true) {
            mostrarMenu();
            int escolha = obterEscolhaDoUsuario();

            if (escolha == 0) {
                System.out.println("Encerrando o programa. Obrigado por usar!");
                break;
            }

            if (escolha >= 1 && escolha <= taxas.size()) {
                String[] moedas = taxas.keySet().toArray(new String[0]);
                String moedaOrigem = moedas[escolha - 1];

                System.out.print("Digite a quantidade para converter: ");
                double quantidade;
                while (!entrada.hasNextDouble()) {
                    System.out.println("Por favor, insira um número válido.");
                    entrada.next();
                }
                quantidade = entrada.nextDouble();
                entrada.nextLine(); // Consumir a nova linha

                System.out.print("Digite a moeda destino (ex: USD, BRL): ");
                String moedaDestino = entrada.nextLine().toUpperCase();

                if (taxas.containsKey(moedaDestino)) {
                    try {
                        double resultado = converter(quantidade, moedaOrigem, moedaDestino);
                        System.out.printf("%.2f %s é igual a %.2f %s%n", quantidade, moedaOrigem, resultado, moedaDestino);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                } else {
                    System.out.println("Moeda destino não suportada.");
                }
            } else {
                System.out.println("Opção inválida. Por favor, tente novamente.");
            }

            System.out.println("Pressione Enter para continuar...");
            entrada.nextLine(); // Aguardar por Enter
        }
        entrada.close();
    }

    private void mostrarMenu() {
        System.out.println("\nEscolha uma moeda para converter:");
        int indice = 1;
        for (String moeda : taxas.keySet()) {
            System.out.println(indice + ". " + moeda);
            indice++;
        }
        System.out.println("0. Sair");
    }

    private int obterEscolhaDoUsuario() {
        System.out.print("Sua escolha: ");
        while (!entrada.hasNextInt()) {
            System.out.println("Por favor, insira um número válido.");
            entrada.next(); // Limpar a entrada inválida
        }
        return entrada.nextInt();
    }

    private double converter(double quantidade, String moedaOrigem, String moedaDestino) {
        if (!taxas.containsKey(moedaOrigem) || !taxas.containsKey(moedaDestino)) {
            throw new IllegalArgumentException("Moeda não suportada para conversão.");
        }

        double taxaOrigem = taxas.get(moedaOrigem);
        double taxaDestino = taxas.get(moedaDestino);

        return quantidade * (taxaDestino / taxaOrigem);
    }

    // Pega as taxas de câmbio da API
    private Map<String, Double> buscarTaxasDeCambio() {
        Map<String, Double> taxasDeCambio = new HashMap<>();
        try {
            // Cria um cliente HTTP
            HttpClient cliente = HttpClient.newHttpClient();
            // Configura a requisição HTTP
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://v6.exchangerate-api.com/v6/%s/latest/USD", CHAVE_API)))
                    .GET()
                    .build();

            // Envia a requisição e espera pela resposta
            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            // Pega a resposta em JSON e transforma em objetos Java
            JsonElement jsonElement = JsonParser.parseString(resposta.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject taxasJson = jsonObject.getAsJsonObject("conversion_rates");

            // Adiciona as taxas de câmbio no mapa
            for (String moeda : taxasJson.keySet()) {
                if (moeda.equals("USD") || moeda.equals("BRL") || moeda.equals("ARS") || moeda.equals("BOB") || moeda.equals("CLP") || moeda.equals("COP")) {
                    taxasDeCambio.put(moeda, taxasJson.get(moeda).getAsDouble());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar taxas de câmbio: " + e.getMessage());
        }
        return taxasDeCambio;
    }

    public static void main(String[] args) {
        new ConversorDeMoedas().iniciar();
    }
}