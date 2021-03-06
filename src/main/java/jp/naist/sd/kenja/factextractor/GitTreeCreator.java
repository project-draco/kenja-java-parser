package jp.naist.sd.kenja.factextractor;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

import jp.naist.sd.kenja.factextractor.ast.ASTCompilation;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

public class GitTreeCreator {
  private Tree root = new Tree("");

  private ASTCompilation compilation;

  public GitTreeCreator() {
  }

  private void parseSourcecode(char[] src, String sourcesDir) {
    ASTParser parser = ASTParser.newParser(AST.JLS4);

    parser.setUnitName("U.java");
    parser.setResolveBindings(true);
    parser.setBindingsRecovery(true);
    String[] sources = sourcesDir.split(",");
    String[] encodings = new String[sources.length];
    for (int i = 0; i < encodings.length; i++) {
      encodings[i] = "UTF-8";
    }
    parser.setEnvironment(null, sources, encodings, true);
    parser.setSource(src);

    SourceFinder.initialize(sources);

    NullProgressMonitor nullMonitor = new NullProgressMonitor();
    CompilationUnit unit = (CompilationUnit) parser.createAST(nullMonitor);

    compilation = new ASTCompilation(unit, root);

  }

  private void parseSourcecodeAndWriteSyntaxTree(char[] src, String outputPath, String sourcesDir, boolean dependencies) {
    File outputFile = new File(outputPath);
    parseSourcecodeAndWriteSyntaxTree(src, outputFile, sourcesDir, dependencies);
  }

  private void parseSourcecodeAndWriteSyntaxTree(char[] src, File outputFile, String sourcesDir, boolean dependencies) {
    parseSourcecode(src, sourcesDir);
    writeASTAsFileTree(outputFile, dependencies);
  }

  private void parseBlobs(String repositoryPath, String syntaxTreeDirPath, String sourceDir, boolean dependencies) {
    File repoDir = new File(repositoryPath);
    try {
      Repository repo = new FileRepository(repoDir);

      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String line;
      while ( (line = br.readLine()) != null) {
        root = new Tree("");

        ObjectId obj = ObjectId.fromString(line);
        ObjectLoader loader = repo.open(obj);

        char[] src = IOUtils.toCharArray(loader.openStream());
        File outputFile = new File(syntaxTreeDirPath, line);
        parseSourcecodeAndWriteSyntaxTree(src, outputFile, sourceDir, dependencies);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void writeASTAsFileTree(File outputFile, boolean dependencies) {
    try {
      TreeWriter writer = new TextFormatTreeWriter(outputFile, dependencies);
      writer.writeTree(compilation.getTree());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length > 3) {
      System.out.println("Usage(1): path_of_output_file");
      System.out.println("Usage(2); path_of_git_repository path_of_syntax_trees_dir");
      return;
    }

    GitTreeCreator creator = new GitTreeCreator();

    if (args[0].equals("--dependencies")) {
      char[] src = IOUtils.toCharArray(System.in);
      creator.parseSourcecodeAndWriteSyntaxTree(src, args[1], args[2], true);
    } else if (args.length == 1) {
      char[] src = IOUtils.toCharArray(System.in);
      creator.parseSourcecodeAndWriteSyntaxTree(src, args[0], "", false);
    } else {
      creator.parseBlobs(args[0], args[1], "", false);
    }
  }
}
