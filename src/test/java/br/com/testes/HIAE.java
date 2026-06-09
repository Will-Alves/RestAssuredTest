package br.com.testes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HIAE {

    private static final Set<String> CAMPOS_ESPERADOS = Set.of(
        "NomePrimeiroFonetico", "IdTabela", "NumProntuario", "NomePessoa",
        "SexoDR", "DescSexo", "NacionalidadeDR", "DescNacionalidade",
        "ReligiaoDR", "DescReligiao", "DataNascimento", "Idade",
        "NumDocumento", "NumCPF", "ResideBrasil", "NumFoneCelular",
        "EmailPessoal", "SMS", "Email", "Estrangeiro",
        "ListaNumCartaoBeneficiario", "PessoaEnderecoDR", "TipoEnderecoDR",
        "DescTipoEndereco", "CepDR", "CodCep", "DescLogradouro",
        "DescEstado", "DescCidade", "DescBairro", "NumEndereco",
        "UsuarioInclusaoDR", "DataInclusao", "HoraInclusao",
        "UsuarioAlteracaoDR", "DataAlteracao", "HoraAlteracao",
        "IdadeAno", "IdadeMes", "IdadeDia", "CompletoPessoa",
        "CompletoPessoaEndereco", "BairroDR", "CidadeEnderecoDR", "EstadoDR",
        "ListaIconografia", "PacienteParRef", "ListaFusao", "RegiaoDR",
        "CodRegiao", "DescRegiao", "DescPais", "Origem", "DataObito",
        "NomePessoaSimplificado", "NaoTemEmail", "Funcionario", "Dependente",
        "DDIPaisDR", "NumDDI", "DDIPaisEstrangeiroDR", "DDIPais2DR",
        "FoneCelular2Principal", "FoneCelularPrincipal", "GeneroDR",
        "CodGenero", "DescGenero",
        "NomeSocial", "NomeSocialFonetico", "NomeSocialSimplificado"
    );

    public static void validarPaciente(Map<String, Object> paciente, int index) {
        System.out.println("\n  Paciente #" + index);

        Metodos_Rest.validarCamposItem(paciente, CAMPOS_ESPERADOS, "Paciente");

        // Identificação
        Metodos_Rest.validarInteiro(paciente, "IdTabela");
        Metodos_Rest.validarInteiro(paciente, "NumProntuario");
        Metodos_Rest.validarString(paciente, "NomePessoa");
        Metodos_Rest.validarString(paciente, "NomePrimeiroFonetico");
        Metodos_Rest.validarString(paciente, "NomePessoaSimplificado");
        Metodos_Rest.validarString(paciente, "NomeSocial");
        Metodos_Rest.validarString(paciente, "NomeSocialFonetico");
        Metodos_Rest.validarString(paciente, "NomeSocialSimplificado");

        // Dados pessoais
        Metodos_Rest.validarString(paciente, "DataNascimento");
        Metodos_Rest.validarInteiro(paciente, "Idade");
        Metodos_Rest.validarInteiro(paciente, "IdadeAno");
        Metodos_Rest.validarString(paciente, "IdadeMes");
        Metodos_Rest.validarInteiro(paciente, "IdadeDia");
        Metodos_Rest.validarString(paciente, "DescSexo");
        Metodos_Rest.validarString(paciente, "DescNacionalidade");
        Metodos_Rest.validarString(paciente, "DescReligiao");
        Metodos_Rest.validarBoolean(paciente, "ResideBrasil", true);
        Metodos_Rest.validarBoolean(paciente, "Estrangeiro", false);

        // Documento
        assertNotNull(paciente.get("NumDocumento"), "NumDocumento não deve ser null");
        assertNotNull(paciente.get("NumCPF"), "NumCPF não deve ser null");

        // Contato
        assertNotNull(paciente.get("NumFoneCelular"), "NumFoneCelular não deve ser null");
        Metodos_Rest.validarString(paciente, "EmailPessoal");
        Metodos_Rest.validarBoolean(paciente, "SMS", true);
        Metodos_Rest.validarBoolean(paciente, "Email", true);

        // Endereço
        Metodos_Rest.validarString(paciente, "CodCep");
        Metodos_Rest.validarString(paciente, "DescLogradouro");
        Metodos_Rest.validarString(paciente, "DescEstado");
        Metodos_Rest.validarString(paciente, "DescCidade");
        Metodos_Rest.validarString(paciente, "DescBairro");
        Metodos_Rest.validarString(paciente, "DescTipoEndereco");

        // Completude
        Metodos_Rest.validarBoolean(paciente, "CompletoPessoa", true);
        Metodos_Rest.validarBoolean(paciente, "CompletoPessoaEndereco", true);

        // Região / origem
        Metodos_Rest.validarString(paciente, "CodRegiao");
        Metodos_Rest.validarString(paciente, "DescRegiao");
        Metodos_Rest.validarString(paciente, "DescPais");
        Metodos_Rest.validarString(paciente, "Origem");

        // Auditoria
        Metodos_Rest.validarString(paciente, "DataInclusao");
        Metodos_Rest.validarString(paciente, "HoraInclusao");
        Metodos_Rest.validarString(paciente, "DataAlteracao");
        Metodos_Rest.validarString(paciente, "HoraAlteracao");

        // Listas
        Object listaIconografia = paciente.get("ListaIconografia");
        assertNotNull(listaIconografia, "ListaIconografia não deve ser null");
        assertTrue(listaIconografia instanceof List<?>, "ListaIconografia deve ser uma lista");
        System.out.println("  ✔ ListaIconografia.size(): " + ((List<?>) listaIconografia).size());

        System.out.println("  ✔ Paciente #" + index + " validado: " + paciente.get("NomePessoa"));
    }
}
