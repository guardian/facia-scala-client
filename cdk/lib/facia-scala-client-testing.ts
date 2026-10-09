import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { FederatedPrincipal, PolicyStatement, Role } from 'aws-cdk-lib/aws-iam';

export class FaciaScalaClientTesting extends GuStack {
    constructor(scope: App, id: string, props: GuStackProps) {
        super(scope, id, props);
        const fapiBucketArn = 'arn:aws:s3:::facia-tool-store';

        const role = new Role(this, 'FaciaScalaClientCIRole', {
            roleName: 'facia-scala-client-ci', // Explicit name for clarity
            assumedBy: new FederatedPrincipal(
                `arn:aws:iam::${this.account}:oidc-provider/token.actions.githubusercontent.com`,
                {
                    StringEquals: {
                        'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
                    },
                    StringLike: {
                        'token.actions.githubusercontent.com:sub':
                            'repo:guardian@164318/facia-scala-client@20723151:*',
                    },
                },
                'sts:AssumeRoleWithWebIdentity',
            ),
        });

        role.addToPolicy(
            new PolicyStatement({
                actions: [
                    's3:GetObject',
                    's3:ListBucket',
                ],
                resources: [
                    `${fapiBucketArn}/DEV/*`,
                    fapiBucketArn,
                ],
            }),
        );

        new CfnOutput(this, 'FaciaScalaClientCIRoleArn', { value: role.roleArn });
    }
}