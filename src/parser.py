import os
from subprocess import (
                            Popen,
                            PIPE,
                        )
from multiprocessing import (
                                Pool,
                                cpu_count
                            )

def execute_parser(cmd, src):
    p = Popen(cmd, stdin=PIPE)
    p.stdin.write(src)
    p.communicate()
    return True

class ParserExecutor:
    parser_class = "jp.naist.sd.kenja.factextractor.ASTGitTreeCreator"

    def __init__(self, output_dir, parser_path, processes=None):
        self.output_dir = output_dir
        self.parser_path = parser_path
        self.processes = processes if processes else cpu_count()
        self.pool = Pool(self.processes)
        self.closed = False

    def parse_blob(self, blob):
        src = blob.data_stream.read()
        cmd = self.make_cmd(blob.hexsha)

        if(self.closed):
            self.pool = Pool(self.processes)
            self.closed = False

        self.pool.apply_async(execute_parser, args=[cmd, src])

    def make_cmd(self, hexsha):
        cmd = ["java",
                "-cp",
                self.parser_path,
                self.parser_class,
                os.path.join(self.output_dir, hexsha)
                ]
        return cmd

    def join(self):
        self.pool.close()
        self.closed = True
        self.pool.join()
        self.pool = None

#if __name__ == "__main__":
#    kenja_jar = "../target/kenja-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
#    kenja_parser_class = "jp.naist.sd.kenja.factextractor.ASTGitTreeCreator"
#    output_dir = "/Users/kenjif/syntax_trees_test/"
#
#    from git import Repo
#    repo = Repo('/Users/kenjif/historage_recover_test/columba_all')
#    exe = ParserExecutor(output_dir, kenja_jar)
#
#    for commit in repo.iter_commits(repo.head):
#        for p in commit.parents:
#            for diff in p.diff(commit):
#                if diff.b_blob and diff.b_blob.name.endswith(".java"):
#                    exe.parse_blob(diff.b_blob)
#
#    exe.join()
